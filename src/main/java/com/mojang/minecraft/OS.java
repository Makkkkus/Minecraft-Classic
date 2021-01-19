package com.mojang.minecraft;

public enum OS
{
	linux("linux", 0),
	solaris("solaris", 1),
	windows("windows", 2),
	macos("macos", 3),
	unknown("unknown", 4);

	private static final OS[] values = new OS[] {linux, solaris, windows, macos, unknown};

	private OS(String name, int id)
	{
	}

	public static int GetOperatingSystemAsInt(String osName) {
		return OperatingSystemLookup.lookup[(osName.contains("win")? OS.windows:
				(osName.contains("mac")? OS.macos:
						(osName.contains("solaris")? OS.solaris:
								(osName.contains("sunos")? OS.solaris:
										(osName.contains("linux")? OS.linux:
												(osName.contains("unix")? OS.linux: OS.unknown)))))).ordinal()];
	}
}
