(ns clang.parser
  (:use [clang.util :only [? ! module]])
  (:require [cljs.reader :refer [read-string]])
  (:require-macros
    [clang.angular :refer [fn-symbol-map]]))

(def fn-syms
  (fn-symbol-map
    count str first last second ffirst rest next fnext nfirst nnext nthnext
    sort reverse deref rand rand-int > < >= <= not= inc dec max min nth get
    clj->js not + - * / rem mod keys vals true? false? nil? = == ))

(defn function [sym]
  (if (keyword? sym)
    sym
    (fn-syms (name sym))))

(defn exec-list [sym args]
  (if-let [f (function sym)]
    (apply f args)
    (str (apply list sym args))))

(defn context-eval [parser form context]
  (cond
    (list? form) (exec-list (first form)
                            (when-let [form (next form)]
                              (map #(context-eval parser % context) form)))
    (symbol? form) (or ((parser (name form)) context)
                       form)
    :else form))

(defn parse [ng-parse data]
  (if-let [data (cond
                  (re-find #"^:\S+$" data)   (str "(" data " $value)")
                  (re-find #"^\(.*\)$" data) data
                  (= \: (first data))        (str "(" data ")")
                  (= \@ (first data))        data)]
    (partial context-eval ng-parse (read-string data))
    (ng-parse data)))

