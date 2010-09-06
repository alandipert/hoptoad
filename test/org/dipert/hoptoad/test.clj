(ns org.dipert.hoptoad.test
  (:use clojure.test
        org.dipert.hoptoad))

(defn make-stacktrace-elem []
  (StackTraceElement. "SomeClass"
                      "someMethod"
                      "somefile.java"
                      234))

(defn make-stacktrace [n]
  (into-array (take n (repeatedly make-stacktrace-elem))))

(defn make-exception [message trace-size]
  (doto (Throwable.)
    (.setStackTrace (make-stacktrace trace-size))))

(deftest t-builds-stacktrace
  (let [error (exception->error (make-exception "Oh noes!" 5))
        lines (map second (rest (second error)))
        first-scrape (first lines)]
    (is (= (count lines) 5))
    (are [x y] (= (x first-scrape) y)
         :method "someMethod"
         :file "somefile.java"
         :number 234)))

(deftest t-make-notice
  (let [notice (make-notice "1234" (make-exception "Snap!" 1))
        elements (filter vector? (rest notice))]
    (is (some #{[:api-key "1234"]} elements))
    (is (some #{:error} (map first elements)))))

