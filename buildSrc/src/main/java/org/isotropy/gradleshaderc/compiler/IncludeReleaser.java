package org.isotropy.gradleshaderc.compiler;

import org.lwjgl.system.MemoryUtil;
import org.lwjgl.util.shaderc.ShadercIncludeResult;
import org.lwjgl.util.shaderc.ShadercIncludeResultReleaseI;

public final class IncludeReleaser implements ShadercIncludeResultReleaseI
{
	@Override
	public void invoke(long user_data, long include_result)
	{
		final var result = ShadercIncludeResult.create(include_result);
		final var content = result.content();
		if (content != null)
		{
			MemoryUtil.memFree(content);
		}
		result.free();
	}
}
