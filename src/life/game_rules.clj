(ns life.game-rules)

(defn neighbours [[x y]]
    (for [dx [-1 0 1] dy [-1 0 1] :when (not= 0 dx dy)]
        [(+ dx x) (+ dy y)]))

(defn life-tick [cells]
    (set (for [[loc n] (frequencies (mapcat neighbours cells))
              :when (or (= n 3) (and (= n 2) (cells loc)))]
    loc)))