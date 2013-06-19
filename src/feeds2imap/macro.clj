(ns feeds2imap.macro)

(defmacro try*
  "Macro to catch multiple exceptions with one catch body.

   Usage:
   (try*
     (println :a)
     (println :b)
     (catch* [A B C] e (println (class e))))

   Will be expanded to:
   (try
     (println :a)
     (println :b)
     (catch A e (println (class e)))
     (catch B e (println (class e)))
     (catch C e (println (class e))))

   (try*
     (println :a)
     (println :b)
     (catch* [A B C] e (println (class e))
     (finally* (println :finally-clause)))

   Will be expanded to:
   (try
     (println :a)
     (println :b)
     (catch A e (println (class e))
     (catch B e (println (class e))
     (catch C e (println (class e))
     (finally (println :finally-clause)
  "
  [& bodies]
  (let [throw-runtime (fn [string] (throw (RuntimeException. string)))
        throw-when (fn [coll fun cnt string]
                     (when (fun (count coll) cnt)
                       (throw-runtime (str "There should be " string " in the try* macro."))))

        catch-clauses (filter #(= (first %) 'catch*) bodies)
        _ (throw-when catch-clauses not= 1 "one catch* clause")

        [[catch-call classes var-name catch-body]] catch-clauses
        _ (throw-when classes < 1 "at least one Exception class")

        catch-clauses (map (fn [cls] `(catch ~cls ~var-name ~catch-body))
                           classes)

        finally-clauses (take-while #(= (first %) 'finally*) (reverse bodies))
        _ (throw-when finally-clauses > 1 "one finally* clause")

        finally-clause (first finally-clauses)
        finally-clause (when finally-clause (cons `finally (rest finally-clause)))

        main-bodies (take-while #(not (contains? #{'catch* 'finally*} (first %))) bodies)
        _ (throw-when main-bodies < 1 "at least one body")

        result (if (seq finally-clause)
                 (concat (list `try) main-bodies catch-clauses (list finally-clause))
                 (concat (list `try) main-bodies catch-clauses))]
    result))
