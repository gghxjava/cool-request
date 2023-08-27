package com.hxl.plugin.springboot.invoke.springmvc;

import com.hxl.plugin.springboot.invoke.springmvc.utils.ParamUtils;
import com.intellij.psi.PsiAnnotation;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiParameter;

import java.util.List;
import java.util.Map;

public class ParameterAnnotationDescriptionUtils {
    private static final List<String> PARAMETER_DESCRIPTION_ANNO =
            List.of("io.swagger.v3.oas.annotations.Parameter#description");

    private static final List<String> METHOD_DESCRIPTION_ANNO =
            List.of("io.swagger.v3.oas.annotations.Operation");

    public static String getParameterDescription(PsiParameter parameter) {
        for (String annotation : PARAMETER_DESCRIPTION_ANNO) {
            String[] split = annotation.split("#");
            String annotationName = split[0];
            PsiAnnotation psiAnnotation = parameter.getAnnotation(annotationName);
            if (psiAnnotation != null) {
                Map<String, String> psiAnnotationValues = ParamUtils.getPsiAnnotationValues(psiAnnotation);
                return psiAnnotationValues.getOrDefault(split[1], "");
            }
        }
        return "";
    }

    public static MethodDescription getMethodDescription(PsiMethod psiMethod) {
        MethodDescription methodDescription = new MethodDescription();
        for (String annotation : METHOD_DESCRIPTION_ANNO) {
            PsiAnnotation psiAnnotation = psiMethod.getAnnotation(annotation);
            if (psiAnnotation != null) {
                Map<String, String> psiAnnotationValues = ParamUtils.getPsiAnnotationValues(psiAnnotation);
                methodDescription.setDescription(psiAnnotationValues.get("description"));
                methodDescription.setSummary(psiAnnotationValues.get("summary"));
                methodDescription.setMethodName(psiMethod.getName());
//                methodDescription.getParameters(psiMethod.getpa)
            }
        }
        return methodDescription;
    }

}
