package org.isotropy.gradleshaderc.compiler;

import static org.lwjgl.util.shaderc.Shaderc.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;

public final class ShadercCompiler implements AutoCloseable
{
	private static final String ENTRY_POINT_NAME = "main";
	public final long compiler;
	public final long options;

	public static ShadercCompiler newCompiler()
	{
		final long compiler = shaderc_compiler_initialize();
		final long options = shaderc_compile_options_initialize();
		shaderc_compile_options_set_optimization_level(	options,
														shaderc_optimization_level_performance);
		shaderc_compile_options_set_target_env(	options,
												shaderc_target_env_vulkan,
												shaderc_env_version_vulkan_1_0);
		return new ShadercCompiler(compiler, options);
	}

	private ShadercCompiler(long compiler, long options)
	{
		this.compiler = compiler;
		this.options = options;
	}

	@Override
	public void close()
	{
		shaderc_compile_options_release(options);
		shaderc_compiler_release(compiler);
	}

	public void compile(File inputFile, File outputFile) throws IOException
	{
		final var shaderCode = Files.readString(inputFile.toPath());
		final var parentDir = inputFile.getParentFile();
		final var includeResolver = new IncludeResolver(parentDir);
		final var includeReleaser = new IncludeReleaser();

		shaderc_compile_options_set_include_callbacks(options, includeResolver, includeReleaser, 0);

		final var fileName = inputFile.getName();
		final int shaderType = findType(fileName);
		final long result = shaderc_compile_into_spv(	compiler,
														shaderCode,
														shaderType,
														fileName,
														ENTRY_POINT_NAME,
														options);

		final int resultId = shaderc_result_get_compilation_status(result);
		if (resultId != shaderc_compilation_status_success)
		{
			final var message = shaderc_result_get_error_message(result);
			System.err.println("Compilation failed (" + resultId + "): " + fileName);
			System.err.println(message);
		}
		else
		{
			final var buffer = shaderc_result_get_bytes(result);
			writeResult(buffer, outputFile);
		}
		shaderc_result_release(result);
	}

	private static int findType(final String fileName)
	{
		int type = shaderc_glsl_infer_from_source;
		if (fileName.endsWith(".comp")) type = shaderc_glsl_compute_shader;
		else if (fileName.endsWith(".vert")) type = shaderc_glsl_vertex_shader;
		else if (fileName.endsWith(".frag")) type = shaderc_glsl_fragment_shader;
		else System.err.println("Unknown type");
		return type;
	}

	private static void writeResult(final ByteBuffer bb, File outputFile) throws IOException
	{
		try (final var outputStream = new FileOutputStream(outputFile, false);
				final var channel = outputStream.getChannel())
		{
			channel.write(bb);
		}
	}
}