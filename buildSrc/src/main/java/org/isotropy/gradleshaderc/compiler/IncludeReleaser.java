package org.isotropy.gradleshaderc.compiler;

import org.lwjgl.util.shaderc.ShadercIncludeResult;
import org.lwjgl.util.shaderc.ShadercIncludeResultReleaseI;

public class IncludeReleaser implements ShadercIncludeResultReleaseI
{
	@Override
	public void invoke(long user_data, long include_result)
	{
		final var result = ShadercIncludeResult.create(include_result);
		result.free();
	}
}
