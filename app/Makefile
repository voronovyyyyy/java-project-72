.PHONY: setup build-css build test lint lint-fix start

GRADLE := ./gradlew
NPM := npm
ifeq ($(OS),Windows_NT)
GRADLE := gradlew.bat
NPM := npm.cmd
endif

setup: build-css
	$(GRADLE) installDist

build-css:
	$(NPM) ci
	$(NPM) run build:css

build: build-css
	$(GRADLE) build shadowJar

test: build-css
	$(GRADLE) test jacocoTestReport

lint:
	$(GRADLE) spotlessCheck

lint-fix:
	$(GRADLE) spotlessApply

start:
	$(NPM) run dev
