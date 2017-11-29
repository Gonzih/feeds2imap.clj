(ns feeds2imap.logging)

(def enabled? true)
(def logger (agent nil))

(defn prn-stdout [_ args]
  (when enabled?
    (apply println args)))

(defn prn-stderr [_ args]
  (when enabled?
    (binding [*out* *err*]
      (apply println args))))

(defn info [& args]
  (send logger prn-stdout args))

(defn error [& args]
  (send logger prn-stderr args))

(defn wait []
  (await-for 3000 logger))
