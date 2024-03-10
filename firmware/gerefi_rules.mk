# Warnings-as-errors...
gerefi_OPT = -Werror
# some compilers seem to have this off by default?
gerefi_OPT += -Werror=stringop-truncation

ifneq ($(ALLOW_SHADOW),yes)
#     gerefi_OPT += -Werror=shadow
endif

# ...except these few
gerefi_OPT += -Wno-error=sign-compare
gerefi_OPT += -Wno-error=overloaded-virtual
gerefi_OPT += -Wno-error=unused-parameter
