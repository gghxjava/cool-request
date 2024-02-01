package com.cool.request.lib.springmvc.param;

import com.cool.request.component.http.net.MediaTypes;
import com.cool.request.lib.springmvc.HttpRequestInfo;
import com.cool.request.lib.springmvc.JSONObjectGuessBody;
import com.cool.request.lib.springmvc.StringGuessBody;
import com.cool.request.lib.springmvc.utils.ParamUtils;
import com.cool.request.utils.PsiUtils;
import com.intellij.openapi.module.ModuleUtil;
import com.intellij.psi.*;
import org.jetbrains.annotations.NotNull;

import java.math.BigInteger;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Supplier;

public class BodyParamSpeculate implements RequestParamSpeculate {
    private final Map<String, Supplier<Object>> defaultValueMap = new HashMap<>();
    private final List<FieldAnnotationDescription> fieldAnnotationDescriptions = new ArrayList<>();

    public BodyParamSpeculate() {
        defaultValueMap.put(Date.class.getName(), Date::new);
        defaultValueMap.put(LocalDateTime.class.getName(), () -> LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME));
        defaultValueMap.put(LocalTime.class.getName(), () -> LocalTime.now().format(DateTimeFormatter.ISO_TIME));
        defaultValueMap.put(LocalDate.class.getName(), () -> LocalDate.now().format(DateTimeFormatter.ISO_DATE));
        defaultValueMap.put(BigInteger.class.getName(), () -> 0);
        fieldAnnotationDescriptions.add(new JacksonFieldAnnotationDescription());

    }

    @Override
    public void set(PsiMethod method, HttpRequestInfo httpRequestInfo) {
        PsiParameter[] parameters = method.getParameterList().getParameters();
        //非GET和具有表单的请求不设置此选项
        if (!ParamUtils.isGetRequest(method) && !ParamUtils.hasMultipartFile(parameters)) {
//            if (!ParamUtils.hasUserObject(method)) return;
//            if (ParamUtils.hasSpringMvcRequestParamAnnotation(method) || ParamUtils.hasBaseType(method)) {
//                new UrlParamSpeculate(true, false).set(method, httpRequestInfo);
//            }
            PsiParameter requestBodyPsiParameter = ParamUtils.getParametersWithAnnotation(method, "org.springframework.web.bind.annotation.RequestBody");
            if (requestBodyPsiParameter == null) {
                //匹配到地一个用户数据类时候返回
                for (PsiParameter parameter : parameters) {
                    String canonicalText = parameter.getType().getCanonicalText();
                    if (ParamUtils.isSpringBoot(canonicalText)) continue;
                    if (ParamUtils.isHttpServlet(canonicalText)) continue;
                    if (ParamUtils.isUserObject(canonicalText)) {
                        PsiClass psiClass = PsiUtils.findClassByName(method.getProject(), ModuleUtil.findModuleForPsiElement(method).getName(), canonicalText);
                        if (psiClass == null) continue;
                        setJsonRequestBody(psiClass, httpRequestInfo);
                        break;
                    }
                }
            } else {
                if (ParamUtils.isString(requestBodyPsiParameter.getType().getCanonicalText())) {
                    httpRequestInfo.setRequestBody(new StringGuessBody(""));
                    httpRequestInfo.setContentType(MediaTypes.TEXT);
                } else if (ParamUtils.isUserObject(requestBodyPsiParameter.getType().getCanonicalText())) {
                    PsiClass psiClass = PsiUtils.findClassByName(method.getProject(), ModuleUtil.findModuleForPsiElement(method).getName(), requestBodyPsiParameter.getType().getCanonicalText());
                    if (psiClass != null) setJsonRequestBody(psiClass, httpRequestInfo);
                }

            }
            //如果是string类型
            if (parameters.length == 1 && ParamUtils.isString(parameters[0].getType().getCanonicalText())) {
                PsiAnnotation requestBody = parameters[0].getAnnotation("org.springframework.web.bind.annotation.RequestBody");
                if (requestBody != null) {
                    httpRequestInfo.setRequestBody(new StringGuessBody(""));
                    httpRequestInfo.setContentType(MediaTypes.TEXT);
                }
            }
        }
    }

    private void setJsonRequestBody(PsiClass psiClass, HttpRequestInfo httpRequestInfo) {
        Map<String, Object> result = new HashMap<>();
        for (PsiField field : listCanApplyJsonField(psiClass)) {
            String fieldName = null;
            for (FieldAnnotationDescription fieldAnnotationDescription : fieldAnnotationDescriptions) {
                fieldName = fieldAnnotationDescription.getRelaName(field);
            }
            if (fieldName == null) fieldName = field.getName();
            result.put(fieldName, getTargetValue(field, new ArrayList<>()));
        }
        httpRequestInfo.setRequestBody(new JSONObjectGuessBody(result));
        httpRequestInfo.setContentType(MediaTypes.APPLICATION_JSON);
    }

    private boolean isInstance(PsiField field) {
        return !field.hasModifierProperty(PsiModifier.STATIC);
    }

    private List<PsiField> listCanApplyJsonField(PsiClass psiClass) {
        List<PsiField> result = new ArrayList<>();
        for (PsiField psiField : psiClass.getAllFields()) {
            if (isInstance(psiField)) result.add(psiField);
        }
        return result;
    }

    private Object getTargetValue(PsiField itemField, List<String> cache) {
        String canonicalText = itemField.getType().getCanonicalText();

        Object defaultValueByClassName = ParamUtils.getDefaultValueByClassName(canonicalText, null);
        if (defaultValueByClassName != null) return defaultValueByClassName;
        if (ParamUtils.isMap(itemField)) return Collections.EMPTY_MAP;
        if (ParamUtils.isArray(canonicalText) || ParamUtils.isList(itemField)) {
            String className = null;

            if (ParamUtils.isArray(canonicalText)) {
                className = canonicalText.replace("[]", "");
            } else {
                className = ParamUtils.getListGenerics(itemField);
                if (className == null) return new ArrayList<>();
            }

            if (ParamUtils.isUserObject(className)) {
                PsiClass psiClass = PsiUtils.findClassByName(itemField.getProject(), ModuleUtil.findModuleForPsiElement(itemField), className);
                if (psiClass != null) {
                    if (cache.contains(psiClass.getQualifiedName())) return new HashMap<>();
                    cache.add(psiClass.getQualifiedName());
                    return List.of(getObjectDefaultValue(psiClass, cache));
                }
                return new ArrayList<>();
            }
            if (ParamUtils.isBaseType(className)) return List.of(ParamUtils.getDefaultValueByClassName(className, ""));
            if (defaultValueMap.containsKey(className)) return List.of(defaultValueMap.get(canonicalText).get());
            return List.of();
        }

        if (defaultValueMap.containsKey(canonicalText)) {
            return defaultValueMap.get(canonicalText).get();
        }
        if (!ParamUtils.isJdkClass(canonicalText)) {
            PsiClass psiClass = PsiUtils.findClassByName(itemField.getProject(),
                    ModuleUtil.findModuleForPsiElement(itemField).getName(), itemField.getType().getCanonicalText());
            if (cache.contains(canonicalText)) return new HashMap<>();
            cache.add(canonicalText);
            return getObjectDefaultValue(psiClass, cache);
        }
        return "";
    }

    @NotNull
    private Object getObjectDefaultValue(PsiClass psiClass, List<String> cache) {
        if (psiClass == null) return "";
        Map<String, Object> result = new HashMap<>();
        for (PsiField field : psiClass.getAllFields()) {
            result.put(field.getName(), getTargetValue(field, cache));
        }
        return result;
    }

}