package com.nachoverdon.mongolia.setup;

import com.nachoverdon.mongolia.templating.MongoliaTemplatingFunctions;
import info.magnolia.module.DefaultModuleVersionHandler;
import info.magnolia.module.InstallContext;
import info.magnolia.module.delta.Task;
import info.magnolia.rendering.module.setup.InstallRendererContextAttributeTask;

import java.util.ArrayList;
import java.util.List;

/**
 * This class is optional and lets you manage the versions of your module,
 * by registering "deltas" to maintain the module's configuration, or other type of content.
 * If you don't need this, simply remove the reference to this class in the module descriptor xml.
 *
 * @see info.magnolia.module.DefaultModuleVersionHandler
 * @see info.magnolia.module.ModuleVersionHandler
 * @see info.magnolia.module.delta.Task
 */
public class MongoliaModuleVersionHandler extends DefaultModuleVersionHandler {
    @Override
    protected List<Task> getExtraInstallTasks(InstallContext installContext) {
        String name = MongoliaTemplatingFunctions.NAME,
            className = MongoliaTemplatingFunctions.class.getName();
        List<Task> extraInstallTasks = new ArrayList<>(super.getExtraInstallTasks(installContext)),
            tasks = new ArrayList<>();

        tasks.add(new InstallRendererContextAttributeTask("rendering", "freemarker", name, className));
        tasks.add(new InstallRendererContextAttributeTask("site", "site", name, className));
        extraInstallTasks.addAll(tasks);

        return extraInstallTasks;
    }
}