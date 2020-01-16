package org.isotropy.gradleshaderc;

import org.gradle.api.Plugin;
import org.gradle.api.Project;

public final class GradleShadercPlugin implements Plugin<Project>
{
	@Override
	public void apply(Project project)
	{
		project.getTasks().create("shadercCompile", GradleShaderc.class);
	}
}
