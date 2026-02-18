LOCAL_PATH:= $(call my-dir)
include $(CLEAR_VARS)
LOCAL_MODULE := libgoldbox-bootstrap
LOCAL_SRC_FILES := goldbox-bootstrap-zip.S goldbox-bootstrap.c
include $(BUILD_SHARED_LIBRARY)
