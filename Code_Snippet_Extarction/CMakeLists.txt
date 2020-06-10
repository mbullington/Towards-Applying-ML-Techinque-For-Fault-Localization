add_library(SkeletonPass MODULE
    # List your source files here.
    Function_Extract_Pass.cpp
)

set(CMAKE_CXX_STANDARD 14)
set(CMAKE_CXX_STANDARD_REQUIRED ON)
set(CMAKE_CXX_EXTENSIONS OFF)

# LLVM is (typically) built with no C++ RTTI. We need to match that;
# otherwise, we'll get linker errors about missing RTTI data.
set_target_properties(SkeletonPass PROPERTIES
    COMPILE_FLAGS "-fno-rtti -std=c++17"
)

# Get proper shared-library behavior (where symbols are not necessarily
# resolved when the shared library is linked) on OS X.
set_target_properties(SkeletonPass PROPERTIES
    LINK_FLAGS "-Wl,-headerpad_max_install_names -undefined dynamic_lookup"
)
