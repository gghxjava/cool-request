package com.hxl.plugin.springboot.invoke.view.page;

import com.hxl.plugin.springboot.invoke.Constant;
import com.hxl.plugin.springboot.invoke.lib.curl.ArgumentHolder;
import com.hxl.plugin.springboot.invoke.lib.curl.BasicCurlParser;
import com.hxl.plugin.springboot.invoke.lib.curl.FileArgumentHolder;
import com.hxl.plugin.springboot.invoke.lib.curl.StringArgumentHolder;
import com.hxl.plugin.springboot.invoke.net.FormDataInfo;
import com.hxl.plugin.springboot.invoke.net.KeyValue;
import com.hxl.plugin.springboot.invoke.net.MediaTypes;
import com.hxl.plugin.springboot.invoke.tool.ProviderManager;
import com.hxl.plugin.springboot.invoke.utils.MediaTypeUtils;
import com.hxl.plugin.springboot.invoke.utils.StringUtils;
import com.hxl.plugin.springboot.invoke.utils.UrlUtils;
import com.hxl.plugin.springboot.invoke.view.BaseTableParamWithToolbar;
import com.hxl.plugin.springboot.invoke.view.IRequestParamManager;
import com.hxl.plugin.springboot.invoke.view.dialog.BigInputDialog;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.ui.table.JBTable;
import org.apache.commons.lang3.tuple.Pair;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

public abstract class BaseJTablePanelWithToolbar extends BaseTableParamWithToolbar {
    protected abstract Object[] getTableHeader();

    protected abstract Object[] getNewRowData();

    protected abstract void initDefaultTableModel(JBTable jTable, DefaultTableModel defaultTableModel);

    private final DefaultTableModel defaultTableModel = new DefaultTableModel(null, getTableHeader());
    private JBTable jTable;
    private final Project project;


    public BaseJTablePanelWithToolbar(Project project) {
        super(project, true);
        this.project = project;
        init();
        showToolBar();
    }

    public Project getProject() {
        return project;
    }

    protected void deleteActionPerformed(ActionEvent e) {
        removeRow();
    }

    @Override
    public void addRow() {
        addNewRow(getNewRowData());
    }

    @Override
    public void copyRow() {
        int selectedRow = jTable.getSelectedRow();
        if (selectedRow != -1) {
            int columnCount = defaultTableModel.getColumnCount();
            Object[] data = new Object[columnCount];
            for (int i = 0; i < columnCount; i++) {
                data[i] = defaultTableModel.getValueAt(selectedRow, i);
            }
            defaultTableModel.addRow(data);
        }

    }

    @Override
    public void removeRow() {
        stopEditor();
        int selectedRow = jTable.getSelectedRow();
        if (selectedRow == -1) return;
        defaultTableModel.removeRow(selectedRow);
        jTable.clearSelection();
        jTable.invalidate();
        jTable.updateUI();
    }

    public void stopEditor() {
        if (jTable.isEditing()) {
            TableCellEditor cellEditor = jTable.getCellEditor();
            cellEditor.stopCellEditing();
            cellEditor.cancelCellEditing();

        }
    }

    public void removeAllRow() {
        stopEditor();
        while (defaultTableModel.getRowCount() > 0) {
            defaultTableModel.removeRow(0);
        }
    }

    protected void addNewRow(Object[] objects) {
        defaultTableModel.addRow(objects);
        jTable.revalidate();
        jTable.invalidate();
    }

    protected void foreachTable(java.util.function.Consumer<List<Object>> consumer) {
        for (int row = 0; row < defaultTableModel.getRowCount(); row++) {
            List<Object> itemRow = new ArrayList<>();
            for (int col = 0; col < defaultTableModel.getColumnCount(); col++) {
                itemRow.add(defaultTableModel.getValueAt(row, col));
            }
            consumer.accept(itemRow);
        }
    }

    @Override
    public void importParam() {
        BigInputDialog bigInputDialog = new BigInputDialog(project);
        bigInputDialog.show();

        try {
            BasicCurlParser.Request parse = new BasicCurlParser().parse(bigInputDialog.getValue());

            //找到参数管理器，设置header、formdata、json参数
            ProviderManager.findAndConsumerProvider(IRequestParamManager.class, project, new Consumer<IRequestParamManager>() {
                @Override
                public void accept(IRequestParamManager iRequestParamManager) {
                    List<KeyValue> header = parse.getHeaders()
                            .stream()
                            .map(pair -> new KeyValue(StringUtils.headerNormalized(pair.getKey()), pair.getValue())).collect(Collectors.toList());
                    //设置请求头
                    iRequestParamManager.setHttpHeader(header);
                    String contentType = iRequestParamManager.getContentTypeFromHeader();


                    List<FormDataInfo> formDataInfos = parse.getFormData().stream().map(stringArgumentHolderPair -> {
                        ArgumentHolder argumentHolder = stringArgumentHolderPair.getValue();
                        String value = "";
                        value = argumentHolder.getName();
                        return new FormDataInfo(stringArgumentHolderPair.getKey(), value, argumentHolder instanceof StringArgumentHolder ? "text" : "file");
                    }).collect(Collectors.toList());

                    //设置form data
                    iRequestParamManager.setFormData(formDataInfos);

                    //1.如果没有设置contentType，但是解析到了form data，则设置为form data
                    if (StringUtils.isEmpty(contentType) && (!formDataInfos.isEmpty())) {
                        contentType = MediaTypes.MULTIPART_FORM_DATA;
                    }
                    //2.如果解析到了form data,其他不需要设置了
                    if (MediaTypes.MULTIPART_FORM_DATA.equalsIgnoreCase(contentType)) {
                        iRequestParamManager.setRequestBodyType(MediaTypes.MULTIPART_FORM_DATA); //剩余的类型都设置为raw文本类型
                        return;
                    }
                    //3.如果解析推测post data是json格式，则设置为json数据
                    if (!StringUtils.isEmpty(parse.getPostData()) && StringUtils.isValidJson(parse.getPostData())) {
                        contentType = MediaTypes.APPLICATION_JSON;
                    }

                    //4. 如果解析推测post data是x-www-form-urlencoded格式，则设置为x-www-form-urlencoded数据

                    //根据contentType的不同，设置不同数据
                    if (MediaTypeUtils.isFormUrlencoded(contentType)) {
                        String postData = parse.getPostData();
                        List<KeyValue> keyValues = UrlUtils.parseFormData(postData);
                        iRequestParamManager.setUrlencodedBody(keyValues);//x-www-form-urlencoded
                    } else if (MediaTypeUtils.isJson(contentType) || MediaTypeUtils.isXml(contentType)) {
                        iRequestParamManager.setRequestBody(contentType, parse.getPostData());//json xml
                        iRequestParamManager.setRequestBodyType(contentType);
                    } else {
                        iRequestParamManager.setRequestBody(MediaTypes.TEXT, parse.getPostData());
                        iRequestParamManager.setRequestBodyType(MediaTypes.TEXT); //剩余的类型都设置为raw文本类型
                    }
                }
            });
        } catch (IllegalArgumentException exception) {
            Messages.showErrorDialog("Unable to parse parameters", "Tip");
        }

    }

    private void init() {
        setLayout(new BorderLayout());
        defaultTableModel.addRow(getNewRowData());
        jTable = new JBTable(defaultTableModel) {
            public boolean isCellEditable(int row, int column) {
                return true;
            }
        };
        jTable.setSelectionBackground(Constant.Colors.TABLE_SELECT_BACKGROUND);
        initDefaultTableModel(jTable, defaultTableModel);
        add(new JScrollPane(jTable), BorderLayout.CENTER);
//        Action delete = new AbstractAction() {
//            public void actionPerformed(ActionEvent e) {
//                JTable table = (JTable) e.getSource();
//                DefaultTableModel model = (DefaultTableModel) table.getModel();
//                int rowCount = model.getRowCount();
//                int emptyValues = 0;
//                for (int i = 0; i < rowCount; i++) {
//                    if (model.getValueAt(i, 0).toString().isEmpty() && model.getValueAt(i, 1).toString().isEmpty()) {
//                        emptyValues++;
//                    }
//                }
//                int modelRow = Integer.parseInt(e.getActionCommand());
//                //如果删除的是空行，并且空行数需要至少一个才能删除
//                if ((model.getValueAt(modelRow, 0).toString().isEmpty() &&
//                        model.getValueAt(modelRow, 1).toString().isEmpty() &&
//                        emptyValues > 1) || (!model.getValueAt(modelRow, 0).toString().isEmpty() || !
//                        model.getValueAt(modelRow, 1).toString().isEmpty())) {
//                    ((DefaultTableModel) table.getModel()).removeRow(modelRow);
//                }
//                if (table.getModel().getRowCount() == 0) {
//                    defaultTableModel.addRow(new String[]{"", "", "text", "Delete"});
//                }
//            }
//        };
//        defaultTableModel.addTableModelListener(e -> {
//            if (e.getType() == TableModelEvent.UPDATE && e.getColumn() == 0
//                    && !defaultTableModel.getValueAt(defaultTableModel.getRowCount() - 1, 0).toString().isEmpty()) {
//                String[] strings = {"", "", "text", "Delete"};
//                defaultTableModel.addRow(strings);
//            }
//        });
    }
}
