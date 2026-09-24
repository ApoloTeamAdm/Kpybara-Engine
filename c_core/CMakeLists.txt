cmake_minimum_required(VERSION 3.15)
project(kpybara_core VERSION 2.1.0 LANGUAGES CXX C)

set(CMAKE_CXX_STANDARD 20)
set(CMAKE_CXX_STANDARD_REQUIRED ON)
set(CMAKE_CXX_EXTENSIONS OFF)

# Flags de Otimização e Segurança Rígidas (C++ Core Guidelines)
if(MSVC)
    add_compile_options(/W4 /WX /permissive- /O2 /GS)
else()
    add_compile_options(
        -Wall
        -Wextra
        -Wpedantic
        -Wconversion
        -Wshadow
        -Wnon-virtual-dtor
        -Wold-style-cast
        -Wcast-align
        -Wunused
        -Woverloaded-virtual
        -O3
        -fPIC
        -fvisibility=hidden
    )
    if(NOT ANDROID)
        add_compile_options(-flto)
    endif()
endif()

# Diretórios de Include
include_directories(
    ${CMAKE_CURRENT_SOURCE_DIR}/include
)

# Alvos: Biblioteca Compartilhada para Dart FFI (.so / .dylib / .dll)
add_library(kpybara_core SHARED
    src/kpybara_core.cpp
    include/kpybara_core.h
    include/kpybara_arena.h
    include/kpybara_watchdog.h
)

target_include_directories(kpybara_core PUBLIC
    $<BUILD_INTERFACE:${CMAKE_CURRENT_SOURCE_DIR}/include>
    $<INSTALL_INTERFACE:include>
)

# Exportação de Símbolos C-ABI
set_target_properties(kpybara_core PROPERTIES
    CXX_VISIBILITY_PRESET hidden
    VISIBILITY_INLINES_HIDDEN ON
    OUTPUT_NAME "kpybara_core"
)

if(UNIX AND NOT APPLE AND NOT ANDROID)
    target_link_libraries(kpybara_core PRIVATE pthread)
endif()

# Alvo Opcional: Biblioteca Estática para testes nativos
add_library(kpybara_core_static STATIC
    src/kpybara_core.cpp
)
target_include_directories(kpybara_core_static PUBLIC
    $<BUILD_INTERFACE:${CMAKE_CURRENT_SOURCE_DIR}/include>
)
