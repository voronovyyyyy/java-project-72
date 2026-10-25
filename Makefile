.PHONY: setup build test lint lint-fix start
setup build test lint lint-fix start:
	$(MAKE) -C app $@
