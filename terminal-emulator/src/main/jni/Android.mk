LOCAL_PATH:= $(call my-dir)
include $(CLEAR_VARS)
LOCAL_MODULE:= libgoldbox
LOCAL_SRC_FILES:= goldbox.c
include $(BUILD_SHARED_LIBRARY)
