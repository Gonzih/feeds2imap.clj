(ns feeds2imap.macro)

(defmacro try*
  "Macro to catch multiple exceptions with one catch body.

   Usage:
   (try*
     (println :a)
     (println :b)
     (catch* [A B] e (println (class e)))
     (catch C e (println :C))
     (finally (println :finally-clause)))

   Will be expanded to:
   (try
     (println :a)
     (println :b)
     (catch A e (println (class e)))
     (catch B e (println (class e)))
     (catch C e (println :C))
     (finally (println :finally-clause)))
  "
  [& body]
  (letfn [(catch*? [form]
            (and (seq form)
                 (= (first form) 'catch*)))
          (expand [[_catch* classes & catch-tail]]
            (map #(list* 'catch % catch-tail) classes))
          (transform [form]
            (if (catch*? form)
              (expand form)
              [form]))]
    (cons 'try (mapcat transform body))))
