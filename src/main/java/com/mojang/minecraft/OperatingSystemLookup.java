package com.mojang.minecraft;

// $FF: synthetic class
final class OperatingSystemLookup {

   // $FF: synthetic field
   static final int[] lookup = new int[OS.values().length];


   static {
      try {
         lookup[OS.linux.ordinal()] = 1;
      } catch (NoSuchFieldError var3) {
         ;
      }

      try {
         lookup[OS.solaris.ordinal()] = 2;
      } catch (NoSuchFieldError var2) {
         ;
      }

      try {
         lookup[OS.windows.ordinal()] = 3;
      } catch (NoSuchFieldError var1) {
         ;
      }

      try {
         lookup[OS.macos.ordinal()] = 4;
      } catch (NoSuchFieldError var0) {
         ;
      }
   }
}
