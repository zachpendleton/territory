(ns territory.game)

; Example
;
; {:id "game-1"
;  :players #{{:id "player-1"
;              :name "Don Draper"
;              :hand #{{:row 2, :col 2}}}
;             {:id "player-2"
;              :name "Peggy Olsen"
;              :hand #{{:row 2, :col 4}}}}
;  :turns (cycle players)
;  :board {:width 6 :height 6}
;  :armies #{
;    {:player "player-1"
;     :tiles #{{:row 1, :col 1}}}
;    {:player "player-2"
;     :tiles #{{:row 3, :col 3}}}
;    ; ...
;  }
;  :claims {
;    [1 1] <army 1>
;    [3 3] <army 2>
;  }
;  :draw '({:row 3, :col 5} ...)

(defn make-draw [{:keys [width height]}]
  (for [row (range width)
        col (range height)]
    [row col]))

(defn new-game [id num-players board]
  {:id id
   :players (with-meta #{} {:awaiting num-players})
   :board board
   :turns nil
   :armies #{}
   :claims {}
   :draw (make-draw board)})

(defn add-player [game player]
  (let [players (:players game)
        awaiting (:awaiting (meta players) 0)]
    (when (pos? awaiting)
      (let [new-players (conj players player)]
        (assoc game
               :players (with-meta new-players
                                   {:awaiting (-> awaiting
                                                  (- (count new-players))
                                                  (+ (count players)))}))))))

(defn shuffle-draw [game]
  (assoc game :draw (shuffle (:draw game))))

(def hand-size 6)

(defn deal-hands [game]
  (reduce (fn [game player]
            (let [hand (take hand-size (:draw game))
                  draw (drop hand-size (:draw game))]
              (assoc game :players (conj (set (:players game))
                                         (assoc player :hand hand))
                     :draw draw)))
          (dissoc game :players)
          (:players game)))

(defn start-game [game]
  (let [awaiting (:awaiting (meta (:players game)) 0)]
    (if-not (pos? awaiting)
      (let [game (deal-hands game)]
        ; This cycle thing is cute, but makes a game hard to inspect in the
        ; REPL, which is lame; will probably just leverage this on the fly with
        ; some sort of turn counter.
        (assoc game :turns (cycle (:players game)))))))
