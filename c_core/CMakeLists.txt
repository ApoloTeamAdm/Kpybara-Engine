# Wrapper de compatibilidade que delega o build para motor/
cmake_minimum_required(VERSION 3.15)
project(kpybara_core_compat LANGUAGES CXX C)

add_subdirectory(${CMAKE_CURRENT_SOURCE_DIR}/../motor ${CMAKE_CURRENT_BINARY_DIR}/motor_build)
