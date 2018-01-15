/*
 * Copyright 2017 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.guvnor.common.services.project.backend.server;

import static org.kie.soup.commons.validation.PortablePreconditions.checkNotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.enterprise.event.Event;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import org.guvnor.common.services.project.events.NewProjectEvent;
import org.guvnor.common.services.project.model.Module;
import org.guvnor.common.services.project.model.POM;
import org.guvnor.common.services.project.model.WorkspaceProject;
import org.guvnor.common.services.project.service.DeploymentMode;
import org.guvnor.common.services.project.service.ModuleService;
import org.guvnor.common.services.project.service.WorkspaceProjectService;
import org.guvnor.structure.organizationalunit.OrganizationalUnit;
import org.guvnor.structure.organizationalunit.OrganizationalUnitService;
import org.guvnor.structure.repositories.Branch;
import org.guvnor.structure.repositories.Repository;
import org.guvnor.structure.repositories.RepositoryEnvironmentConfigurations;
import org.guvnor.structure.repositories.RepositoryService;
import org.uberfire.backend.server.util.Paths;
import org.uberfire.backend.vfs.Path;
import org.uberfire.spaces.Space;
import org.uberfire.spaces.SpacesAPI;

public class WorkspaceProjectServiceImpl
        implements WorkspaceProjectService {

    private OrganizationalUnitService organizationalUnitService;
    private RepositoryService repositoryService;
    private Event<NewProjectEvent> newProjectEvent;
    private ModuleService<? extends Module> moduleService;
    private SpacesAPI spaces;

    public WorkspaceProjectServiceImpl() {
    }

    @Inject
    public WorkspaceProjectServiceImpl(final OrganizationalUnitService organizationalUnitService,
                                       final RepositoryService repositoryService,
                                       final SpacesAPI spaces,
                                       final Event<NewProjectEvent> newProjectEvent,
                                       final Instance<ModuleService<? extends Module>> moduleServices) {
        this.organizationalUnitService = organizationalUnitService;
        this.repositoryService = repositoryService;
        this.spaces = spaces;
        this.newProjectEvent = newProjectEvent;
        moduleService = moduleServices.get();
    }

    @Override
    public Collection<WorkspaceProject> getAllWorkspaceProjects() {

        final List<WorkspaceProject> result = new ArrayList<>();

        for (final OrganizationalUnit ou : organizationalUnitService.getOrganizationalUnits()) {
            result.addAll(getAllWorkspaceProjects(ou));
        }

        return result;
    }

    @Override
    public Collection<WorkspaceProject> getAllWorkspaceProjects(final OrganizationalUnit organizationalUnit) {
        final List<WorkspaceProject> result = new ArrayList<>();

        for (final Repository repository : repositoryService.getAllRepositoriesFromAllUserSpaces()) {

            if (containsRepository(organizationalUnit, repository)
                    && repository.getDefaultBranch().isPresent()) {

                result.add(new WorkspaceProject(organizationalUnit,
                                                repository,
                                                repository.getDefaultBranch().get(),
                                                moduleService.resolveModule(repository.getDefaultBranch().get().getPath())));
            }
        }

        return result;
    }

    private boolean containsRepository(final OrganizationalUnit organizationalUnit,
                                       final Repository repository) {
        for (final Repository ouRepository : organizationalUnitService.getOrganizationalUnit(organizationalUnit.getName()).getRepositories()) {
            if (ouRepository.getAlias().equals(repository.getAlias())) {
                return true;
            }
        }

        return false;
    }

    @Override
    public WorkspaceProject newProject(final OrganizationalUnit organizationalUnit,
                                       final POM pom) {
        return newProject(organizationalUnit,
                          pom,
                          DeploymentMode.VALIDATED);
    }

    @Override
    public WorkspaceProject newProject(final OrganizationalUnit organizationalUnit,
                                       final POM pom,
                                       final DeploymentMode mode) {

        final Repository repository = repositoryService.createRepository(organizationalUnit,
                                                                         "git",
                                                                         checkNotNull("project name in pom model",
                                                                                      pom.getName()),
                                                                         new RepositoryEnvironmentConfigurations());

        if (!repository.getDefaultBranch().isPresent()) {
            throw new IllegalStateException("New repository should always have a branch.");
        }

        final Module module = moduleService.newModule(repository.getDefaultBranch().get().getPath(),
                                                      pom,
                                                      "",
                                                      mode);

        final WorkspaceProject workspaceProject = new WorkspaceProject(organizationalUnit,
                                                                       repository,
                                                                       repository.getDefaultBranch().get(),
                                                                       module);

        newProjectEvent.fire(new NewProjectEvent(workspaceProject));

        return workspaceProject;
    }

    @Override
    public WorkspaceProject resolveProject(final Repository repository) {

        if (!repository.getDefaultBranch().isPresent()) {
            throw new IllegalStateException("New repository should always have a branch.");
        }

        return resolveProject(repository.getSpace(), repository.getDefaultBranch().get());
    }

    @Override
    public WorkspaceProject resolveProject(final Space space, final Branch branch) {
        return resolveProject(space, branch.getPath());
    }

    @Override
    public WorkspaceProject resolveProject(final Space space, final Module module) {
        return resolveProject(space, module.getRootPath());
    }

    @Override
    public WorkspaceProject resolveProject(final Space space, final String name) {

        OrganizationalUnit ou = organizationalUnitService.getOrganizationalUnit(space.getName());
        return resolveProject(ou, name);
    }

    private WorkspaceProject resolveProject(OrganizationalUnit ou, final String name) {
        for (final WorkspaceProject workspaceProject : getAllWorkspaceProjects(ou)) {
            if (workspaceProject.getName().equals(name)) {
                return workspaceProject;
            }
        }

        return null;
    }

    @Override
    public WorkspaceProject resolveProjectByRepositoryAlias(final Space space, final String repositoryAlias) {
        return resolveProject(repositoryService.getRepositoryFromSpace(space, repositoryAlias));
    }

    @Override
    public WorkspaceProject resolveProject(final Space space, final Path path) {

        final org.uberfire.java.nio.file.Path repositoryRoot = Paths.convert(path).getRoot();

        final Repository repository = repositoryService.getRepository(space, Paths.convert(repositoryRoot));

        final Branch branch = resolveBranch(repositoryRoot,
                                            repository);

        return new WorkspaceProject(organizationalUnitService.getOrganizationalUnit(repository.getSpace().getName()),
                                    repository,
                                    branch,
                                    moduleService.resolveModule(Paths.convert(Paths.convert(branch.getPath()).getRoot())));
    }

    @Override
    public WorkspaceProject resolveProject(Path path) {
        return spaces
                .resolveSpace(path.toURI())
                .map(space -> resolveProject(space, path))
                .orElseThrow(() -> new IllegalArgumentException("Could not determine space containing path: " + path));
    }

    private Branch resolveBranch(final org.uberfire.java.nio.file.Path repositoryRoot,
                                 final Repository repository) {

        final Branch defaultBranch = repository.getDefaultBranch().get();

        if (!Paths.convert(defaultBranch.getPath()).equals(repositoryRoot)) {

            for (final Branch branch : repository.getBranches()) {

                if (Paths.convert(branch.getPath()).equals(repositoryRoot)) {
                    return branch;
                }
            }
        }
        return defaultBranch;
    }
}
