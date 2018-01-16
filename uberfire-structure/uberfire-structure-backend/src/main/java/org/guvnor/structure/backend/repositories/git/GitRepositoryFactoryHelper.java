/*
 * Copyright 2015 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.guvnor.structure.backend.repositories.git;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.guvnor.structure.repositories.EnvironmentParameters;
import org.guvnor.structure.repositories.Repository;
import org.guvnor.structure.server.config.ConfigGroup;
import org.guvnor.structure.server.config.ConfigItem;
import org.guvnor.structure.server.config.PasswordService;
import org.guvnor.structure.server.repositories.RepositoryFactoryHelper;
import org.uberfire.io.IOService;
import org.uberfire.spaces.SpacesAPI;

import static org.guvnor.structure.repositories.impl.git.GitRepository.SCHEME;
import static org.kie.soup.commons.validation.Preconditions.checkNotNull;

@ApplicationScoped
public class GitRepositoryFactoryHelper implements RepositoryFactoryHelper {

    private IOService indexedIOService;

    private IOService notIndexedIOService;

    private SpacesAPI spacesAPI;

    @Inject
    private PasswordService secureService;

    public GitRepositoryFactoryHelper() {
    }

    @Inject
    public GitRepositoryFactoryHelper(@Named("ioStrategy") IOService indexedIOService,
                                      @Named("configIO") IOService notIndexedIOService,
                                      SpacesAPI spacesAPI) {
        this.indexedIOService = indexedIOService;
        this.notIndexedIOService = notIndexedIOService;
        this.spacesAPI = spacesAPI;
    }

    @Override
    public boolean accept(final ConfigGroup repoConfig) {
        checkNotNull("repoConfig",
                     repoConfig);
        final ConfigItem<String> schemeConfigItem = repoConfig.getConfigItem(EnvironmentParameters.SCHEME);
        checkNotNull("schemeConfigItem",
                     schemeConfigItem);
        return SCHEME.toString().equals(schemeConfigItem.getValue());
    }

    @Override
    public Repository newRepository(final ConfigGroup repoConfig) {

        validate(repoConfig);

        ConfigItem<String> sValue = repoConfig.getConfigItem(EnvironmentParameters.AVOID_INDEX);

        if (sValue != null && Boolean.valueOf(sValue.getValue())) {
            return new GitRepositoryBuilder(notIndexedIOService,
                                            secureService,
                                            spacesAPI).build(repoConfig);
        }

        return new GitRepositoryBuilder(indexedIOService,
                                        secureService,
                                        spacesAPI).build(repoConfig);
    }

    private void validate(ConfigGroup repoConfig) {
        checkNotNull("repoConfig",
                     repoConfig);
        final ConfigItem<String> schemeConfigItem = repoConfig.getConfigItem(EnvironmentParameters.SCHEME);
        checkNotNull("schemeConfigItem",
                     schemeConfigItem);
    }
}
