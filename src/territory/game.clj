(ns territory.game)

; Example
;
; {:id "game-1"
;  :players [{:id "player-1"
;              :name "Don Draper"
;              :hand #{{:row 2, :col 2}}}
;             {:id "player-2"
;              :name "Peggy Olsen"
;              :hand #{{:row 2, :col 4}}}]
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
;  :played-tiles []
;  :draw '({:row 3, :col 5} ...)

(defn make-draw [{:keys [width height]}]
  (for [row (range width)
        col (range height)]
    [row col]))

(defn new-game [id num-players board]
  {:id id
   :players (with-meta [] {:awaiting num-players})
   :board board
   :played-tiles []
   :claims {}
   :armies #{}
   :draw (make-draw board)})

(defn ^:dynamic handle-extra-player [game _]
  game)

(defn add-player [game player]
  (let [players (:players game)
        awaiting (:awaiting (meta players) 0)]
    (if (pos? awaiting)
      (let [new-players (distinct (conj players (atom player)))]
        (assoc game
               :players (with-meta new-players
                                   {:awaiting (-> awaiting
                                                  (- (count new-players))
                                                  (+ (count players)))})))
      (handle-extra-player game player))))

(defn shuffle-draw [game]
  (assoc game :draw (shuffle (:draw game))))

(defn shuffle-players [game]
  (let [players (:players game)]
    (assoc game :players (with-meta (shuffle players) (meta players)))))

(defn ^:dynamic handle-not-enough-cards [game _]
  game)

(defn deal-hands [{:keys [players draw] :as game} hand-size]
  (if (<= (* (count players) hand-size) (count draw))
    (reduce (fn [{:keys [players draw] :as game} player]
              (let [hand (take hand-size draw)
                    new-draw (drop hand-size draw)]
                (assoc game
                       :players (conj players (swap! player assoc :hand hand))
                       :draw new-draw)))
            (assoc game :players [])
            players)
    (handle-not-enough-cards game hand-size)))

(defn start-game [game hand-size]
  (let [awaiting (:awaiting (meta (:players game)) 0)]
    (when-not (pos? awaiting) (deal-hands game hand-size))))

(defn current-player [{:keys [played-tiles players] :as game}]
  (->> (cycle players)
       (drop (count played-tiles))
       first))

(defn play-tile [{:keys [armies claims played-tiles] :as game} tile]
  (let [location ((juxt :row :col) tile)
        neighboring-armies (into #{} (map #(get claims %) [[0 -1] [1 0] [0 1] [-1 0]]))
        largest-army (last (sort-by (comp count :tiles) neighboring-armies))
        remaining-armies (remove #{largest-army} armies)
        army {:player (:favor tile (current-player game))
              :tiles #{location}}]
    (assoc game
           :played-tiles (conj played-tiles tile)
           :claims (assoc claims location army)
           :armies (conj remaining-armies (assoc army :tiles (clojure.set/union (:tiles army) (:tiles largest-army)))))))
