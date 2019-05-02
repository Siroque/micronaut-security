/*
 * Copyright 2017-2019 original authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.micronaut.security.oauth2.endpoint.endsession;

import io.micronaut.context.BeanContext;
import io.micronaut.context.annotation.Requires;
import io.micronaut.http.HttpRequest;
import io.micronaut.inject.qualifiers.Qualifiers;
import io.micronaut.security.authentication.Authentication;
import io.micronaut.security.oauth2.configuration.endpoints.EndSessionConfiguration;
import io.micronaut.security.oauth2.endpoint.endsession.request.EndSessionRequest;
import io.micronaut.security.oauth2.endpoint.token.response.OauthUserDetailsMapper;
import io.micronaut.views.ModelAndView;
import io.micronaut.views.model.ViewModelProcessor;

import javax.annotation.Nonnull;
import javax.inject.Singleton;
import java.util.HashMap;
import java.util.Map;

/**
 * {@link ViewModelProcessor} which adds to the model the end session url.
 *
 * @author Sergio del Amo
 * @since 1.0.0
 */
@Requires(classes = ViewModelProcessor.class)
@Singleton
public class EndSessionViewModelProcessor implements ViewModelProcessor {

    private final BeanContext beanContext;
    private final EndSessionConfiguration endSessionConfiguration;

    public EndSessionViewModelProcessor(BeanContext beanContext,
                                        EndSessionConfiguration endSessionConfiguration) {
        this.beanContext = beanContext;
        this.endSessionConfiguration = endSessionConfiguration;
    }

    @Override
    public void process(@Nonnull HttpRequest<?> request, @Nonnull ModelAndView<Map<String, Object>> modelAndView) {
        request.getUserPrincipal(Authentication.class).ifPresent(authentication -> {
            Object provider = authentication.getAttributes().get(OauthUserDetailsMapper.PROVIDER_KEY);
            if (provider != null) {
                beanContext.findBean(EndSessionRequest.class, Qualifiers.byName(provider.toString()))
                        .ifPresent(endSessionRequest -> {
                            Map<String, Object> model = modelAndView.getModel().orElseGet(() -> new HashMap<>(1));
                            model.putIfAbsent(endSessionConfiguration.getViewModelKey(), endSessionRequest.getUrl(request, authentication));
                            modelAndView.setModel(model);
                        });
            }
        });
    }
}