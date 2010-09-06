(ns org.dipert.hoptoad
  "Hoptoad notifications for Clojure apps")

;; (spit "/tmp/lol"
;;       (binding [*prxml-indent* 2]
;;         (with-out-str
;;           (prxml [:decl! "1.0"] (make-notice "1234" (Exception. "LOL!" ))))))

(def notifier
  {:name "Clojure Hoptoad Notifier"
   :version "1.0.0"
   :url "http://github.com/alandipert/hoptoad"})

(defn make-notice [api-key exception]
     [:notice {:version "2.0"}
      [:api-key api-key]
      [:notifier
       [:name "Clojure Hoptoad Notifier"]
       [:version "1.0.0"]
       [:url "http://github.com/alandipert/hoptoad"]]
      (exception->error exception)
      [:request
       [:url "http://bob.net/lol"]
       [:component]
       [:action]
       [:cgi-data
        [:var {:key "SERVER_NAME"} "bob.net"]
        [:var {:key "HTTP_USER_AGENT"} "Mozilla"]]]
      [:server-environment
       [:project-root "/testapp"]
       [:environment-name "production"]]])

(defn backtrace-scrape? [s]
  (and (sequential? s)
       (every? map? s)
       (every? #(= #{:method :file :number} (set (keys %))) s)))

(defn scrape-backtrace
  "Returns a lazy seq of backtrace maps, each with method, line number,
  and file name"
  [e]
  {:pre [(instance? Throwable e)]
   :post [(backtrace-scrape? %)]}
  (map #(hash-map
         :method (.getMethodName %)
         :number (.getLineNumber %)
         :file (str (.getClassName %) "(" (.getFileName %) ")"))
       (.getStackTrace e)))

(defn build-lines
  "Returns the line elements for the backtrace."
  [trace-map-seq]
  {:pre [(backtrace-scrape? trace-map-seq)]
   :post [(every? vector? %)
          (every? (fn [elem]
                    (and (= (first elem) :line)
                         (map? (second elem))))
                  %)]}
  (map #(vector :line %) trace-map-seq))

(defn exception->error
  "Returns a full error element with backtrace."
  [e]
  {:pre [(instance? Throwable e)]
   :post [(= :error (first %))
          (= :backtrace (first (second %)))]}
  [:error
   (into [:backtrace] (-> e scrape-backtrace build-lines))])

