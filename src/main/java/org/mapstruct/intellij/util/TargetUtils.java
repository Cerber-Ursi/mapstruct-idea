/*
 *  Copyright 2017 the MapStruct authors (http://www.mapstruct.org/)
 *  and/or other contributors as indicated by the @authors tag. See the
 *  copyright.txt file in the distribution for a full listing of all
 *  contributors.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.mapstruct.intellij.util;

import java.util.stream.Stream;

import com.intellij.openapi.util.Pair;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiParameter;
import com.intellij.psi.PsiSubstitutor;
import com.intellij.psi.util.PsiUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static org.mapstruct.intellij.util.MapstructUtil.canDescendIntoType;

/**
 * Utils for working with target properties (extracting targets  for MapStruct).
 *
 * @author Filip Hrisafov
 */
public class TargetUtils {

    private TargetUtils() {
    }

    /**
     * Get the relevant class for the {@code mappingMethod}. This can be the return of the method, the parameter
     * annotated with {@link org.mapstruct.MappingTarget}, or {@code null}
     *
     * @param mappingMethod the mapping method
     *
     * @return the target class for the given {@code mappingMethod}
     */
    @Nullable
    public static PsiClass getRelevantClass(@NotNull PsiMethod mappingMethod) {
        //TODO here we need to take into consideration both with @MappingTarget and return,
        // returning an interface etc.
        if ( !canDescendIntoType( mappingMethod.getReturnType() ) ) {
            return null;
        }
        PsiClass psiClass = PsiUtil.resolveClassInType( mappingMethod.getReturnType() );
        if ( psiClass == null ) {
            psiClass = Stream.of( mappingMethod.getParameterList().getParameters() )
                .filter( MapstructUtil::isMappingTarget )
                .findAny()
                .map( PsiParameter::getType )
                .filter( MapstructUtil::canDescendIntoType )
                .map( PsiUtil::resolveClassInType )
                .orElse( null );
        }
        return psiClass;
    }

    /**
     * Extract all public setters with their psi substitutors from the given {@code psiClass}
     *
     * @param psiClass to use to extract the setters
     *
     * @return a stream that holds all public setters for the given {@code psiClass}
     */
    public static Stream<Pair<PsiMethod, PsiSubstitutor>> publicSetters(@NotNull PsiClass psiClass) {
        return psiClass.getAllMethodsAndTheirSubstitutors().stream()
            .filter( pair -> MapstructUtil.isSetter( pair.getFirst() ) )
            .filter( pair -> MapstructUtil.isPublic( pair.getFirst() ) );
    }
}
