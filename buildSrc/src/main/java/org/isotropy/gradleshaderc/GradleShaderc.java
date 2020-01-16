package org.isotropy.gradleshaderc;

import java.io.File;
import java.io.IOException;

import org.gradle.api.DefaultTask;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.file.FileType;
import org.gradle.api.tasks.InputDirectory;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.PathSensitive;
import org.gradle.api.tasks.PathSensitivity;
import org.gradle.api.tasks.TaskAction;
import org.gradle.work.ChangeType;
import org.gradle.work.FileChange;
import org.gradle.work.Incremental;
import org.gradle.work.InputChanges;
import org.isotropy.gradleshaderc.compiler.ShadercCompiler;

public abstract class GradleShaderc extends DefaultTask
{
	@Incremental
	@PathSensitive(PathSensitivity.NAME_ONLY)
	@InputDirectory
	abstract DirectoryProperty getInputDir();

	@OutputDirectory
	abstract DirectoryProperty getOutputDir();

	@TaskAction
	void compileShader(InputChanges inputChanges)
	{
		final var inputDir = getInputDir();
		final var outputDir = getOutputDir();

		try (final var compiler = ShadercCompiler.newCompiler())
		{
			for (final var change : inputChanges.getFileChanges(inputDir))
			{
				if (change.getFileType() == FileType.FILE)
				{
					changedFile(compiler, change, outputDir);
				}
			}
		} catch (final Exception e)
		{
			e.printStackTrace();
		}
	}

	private static void changedFile(ShadercCompiler compiler,
									FileChange change,
									DirectoryProperty outputDir)
			throws IOException
	{
		final var targetFile = getTargetFile(change, outputDir);
		if (change.getChangeType() == ChangeType.REMOVED)
		{
			targetFile.delete();
		}
		else
		{
			final var file = change.getFile();
			if (file.getName().endsWith(".glsl") == false)
			{
				compiler.compile(file, targetFile);
			}
		}
	}

	private static File getTargetFile(FileChange change, DirectoryProperty outputDir)
	{
		final var normalizedPath = change.getNormalizedPath() + ".spv";
		final var targetFile = outputDir.file(normalizedPath).get().getAsFile();
		return targetFile;
	}
}
