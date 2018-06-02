# OWLToECharts

## Introduction

OWL ontology to JSON file parser.

## Usage

Edit the constructor and execute Parse() method.

## Issue

The parser takes individuals which has property src:AbstractService and is not src:Part of other individuals as root nodes, and recursively locate src:Instantiation, src:Part and src:Components as children nodes.
