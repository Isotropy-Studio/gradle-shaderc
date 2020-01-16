package org.isotropy.gradleshaderc.compiler;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import org.lwjgl.system.MemoryUtil;
import org.lwjgl.util.shaderc.ShadercIncludeResolveI;
import org.lwjgl.util.shaderc.ShadercIncludeResult;

public class IncludeResolver implements ShadercIncludeResolveI
{
	private final File directory;

	public IncludeResolver(File directory)
	{
		this.directory = directory;
	}

	@Override
	public long invoke(	long user_data,
						long requested_source,
						int type,
						long requesting_source,
						long include_depth)
	{
		final var result = ShadercIncludeResult.calloc();
		final var fileName = MemoryUtil.memUTF8(requested_source);
		for (final var neighbor : directory.listFiles())
		{
			if (neighbor.getName().equals(fileName))
			{
				try
				{
					final String code = Files.readString(neighbor.toPath());
					final var buffer = MemoryUtil.memUTF8(code, false);
					result.content(buffer);
					result.source_name(MemoryUtil.memByteBufferNT1(requested_source));
				} catch (final IOException e)
				{
					e.printStackTrace();
				}
				break;
			}
		}

		return result.address();
	}
}
