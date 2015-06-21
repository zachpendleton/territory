(ns territory.game-test
  (:require [clojure.test :refer :all]
            [territory.game :as game]))

(deftest start-game-test
  (let [new-game (game/new-game "abc123" 2 {:width 5 :height 5})]
    (is (empty? (:players new-game))
        "has an empty players seq")
    (is (= 2 (:awaiting (meta (:players new-game))))
        "is awaiting the specified number of players")
    (is (= {:width 5 :height 5} (:board new-game))
        "sets board dimensions")
    (is (= 25 (count (:draw new-game)))
        "sets draw pile correctly")))

(deftest add-player-test
  (let [game (game/new-game "abc123" 2 {:width 5 :height 5})
        player-a {:id "a"}
        player-b {:id "b"}
        player-c {:id "c"}]

    (is (= 1 (count (:players (game/add-player game player-a))))
        "adds the player")
    (is (= 1 (:awaiting (meta (:players (game/add-player game player-a)))))
        "decrements the awaited-players count")

    (is (= 1 (count (:players (-> game (game/add-player player-a) (game/add-player player-a)))))
        "doesn't add the same player more than once")
    (is (= 1 (:awaiting (meta (:players (-> game (game/add-player player-a) (game/add-player player-a))))))
        "doesn't adjust the awaited-players count when given a duplicate player")

    (let [full-game (-> game (game/add-player player-a) (game/add-player player-b))]
      (is (= full-game (game/add-player full-game player-c))
          "ignores extra players by default")

      (binding [game/handle-extra-player #(hash-map :game %1 :player %2)]
        (is (= full-game (:game (game/add-player full-game player-c)))
            "custom handler receives the original game")
        (is (= player-c (:player (game/add-player full-game player-c)))
            "custom handler receives the extra player")))))

(deftest deal-hands-test
  (let [game (assoc (game/new-game "abc123" 2 {:width 5 :height 5})
                    :players (into [] (map atom [{:id "a"} {:id "b"}]))
                    :draw [{:row 1 :col 1} {:row 1 :col 2} {:row 1 :col 3}
                           {:row 2 :col 1} {:row 2 :col 2} {:row 2 :col 3}])]
    (is (= [[{:row 1 :col 1} {:row 1 :col 2} {:row 1 :col 3}]
            [{:row 2 :col 1} {:row 2 :col 2} {:row 2 :col 3}]]
           (map :hand (:players (game/deal-hands game 3))))
        "deals hands to each player")
    (is (empty? (:draw (game/deal-hands game 3)))
        "removes dealt cards from draw pile")

    (is (= game (game/deal-hands game 6))
        "deals no cards when hand size is too big by default")

    (binding [game/handle-not-enough-cards #(hash-map :game %1 :hand-size %2)]
      (is (= game (:game (game/deal-hands game 6)))
          "custom handler receives the original game")
      (is (= 6 (:hand-size (game/deal-hands game 6)))
          "custom handler receives the specified hand size"))))

(deftest current-player-test
  (let [game (assoc (game/new-game "abc123" 3 {:width 3 :height 3})
                    :players (into [] (map atom [{:id "a"} {:id "b"} {:id "c"}])))]
    (is (= {:id "a"} @(game/current-player game))
        "new game has first player as current")
    (is (= {:id "c"} @(game/current-player (assoc game :played-tiles [:tile-1 :tile-2])))
        "game with played tiles returns next player to play")
    (is (= {:id "a"} @(game/current-player (assoc game :played-tiles [:tile-1 :tile-2 :tile-3])))
        "loops around to beginning of player list")))

(deftest play-tile-test
  (let [game (assoc (game/new-game "abc123" 2 {:width 3 :height 3})
                    :players (into [] (map atom [{:id "a" :hand [{:row 0, :col 0}]}
                                                 {:id "b" :hand [{:row 2, :col 2}]}])))
        current-player (-> game :players first)
        tile (-> current-player :hand first)]
    (is (= 1 (count (:armies (game/play-tile game tile))))
        "playing a tile in an empty board creates an army")
    (is (= 2 (count (:armies (-> game (game/play-tile tile) (game/play-tile {:row 2, :col 2})))))
        "playing a tile in an unoccupied region of the board creates another army")
    (is (= 1 (:claims (-> game (game/play-tile tile) (game/play-tile {:row 0, :col 1}))))
        "playing a tile next to an occupied space joins it into one army")))

(deftest absorb-test
  (testing "first army absorbs second army's cells"
    (is (= {:player 1 :cells #{1 2 3}}
           (absorb {:player 1 :cells #{1 3}}
                   {:player 2 :cells #{2}})))))

(deftest neighboring-armies-test
  (testing "retrieving neighbors from an empty board returns an empty set"
    (is (= #{}
           (neighboring-armies {} {} [1 1]))))
  (testing "retrieving neighbors only returns unique armies"
    (is (= #{:an-army :another-army}
           (neighboring-armies {[1 2] 1
                                [2 1] 2
                                [1 0] 2
                                [0 1] 1}
                               {1 :an-army 2 :another-army}
                               [1 1])))))

(deftest largest-armies-test
  (testing "get largest armies"
    (is (= [{:cells [1 2 3]} {:cells [4 5 6]}]
           (largest-armies [{:cells [1 2 3]}
                            {:cells [1 2]}
                            {:cells [1]}
                            {:cells [4 5 6]}])))))

(deftest pick-winning-army-test
  (testing "returns a single winning army regardless of favor"
    (is (= {:player 1 :cells [1 2 3]}
           (pick-winning-army [{:player 1 :cells [1 2 3]}
                               {:player 2 :cells [1]}]
                              2))))
  (testing "returns an army matching the favor if there are two tied"
    (is (= {:player 2 :cells [4 5 6]}
           (pick-winning-army [{:player 1 :cells [1 2 3]}
                               {:player 2 :cells [4 5 6]}]
                              2))))
  (testing "returns nil if no disputed winner matches favor"
    (is (= nil
           (pick-winning-army [{:player 1 :cells [1 2 3]}
                               {:player 2 :cells [4 5 6]}]
                              3)))))

(deftest deploy-army-test
  (testing "playing a tile into an empty field"
    (let [field {}
          armies {}
          game {:field field :armies armies}
          army {:id "a-1" :player "p-1" :cells #{[1 1]}}
          new-game (deploy-army game army "p-1")]
      (is (= {[1 1] "a-1"}
             (:field new-game)))
      (is (= {"a-1" {:id "a-1" :player "p-1" :cells #{[1 1]}}}
             (:armies new-game)))))
  (testing "playing a tile into a non-empty field"
    (let [field {[1 1] "a-1"}
          armies {"a-1" {:id "a-1" :player "p-1" :cells #{[1 1]}}}
          game {:field field :armies armies}
          army {:id "a-2" :player "p-2" :cells #{[3 1]}}
          new-game (deploy-army game army "p-2")]
      (is (= {[1 1] "a-1", [3 1] "a-2"}
             (:field new-game)))
      (is (= {"a-1" {:id "a-1" :player "p-1" :cells #{[1 1]}}
              "a-2" {:id "a-2" :player "p-2" :cells #{[3 1]}}}
             (:armies new-game)))))
  (testing "playing a tile next to an existing size-one army"
    (let [field {[1 1] "a-1"}
          armies {"a-1" {:id "a-1" :player "p-1" :cells #{[1 1]}}}
          game {:field field :armies armies}
          army {:id "a-2" :player "p-2" :cells #{[2 1]}}
          new-game (deploy-army game army "p-2")]
      (is (= {[1 1] "a-2", [2 1] "a-2"}
             (:field new-game)))
      (is (= {"a-2" {:id "a-2" :player "p-2" :cells #{[1 1] [2 1]}}}
             (:armies new-game))))))
