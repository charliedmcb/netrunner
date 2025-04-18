(ns game.core.abilities-test
  (:require
   [clojure.test :refer :all]
   [game.cards.ice :as ice]
   [game.core :as core]
   [game.core.card :refer :all]
   [game.core.card-defs :refer :all]
   [game.core.eid :as eid]
   [game.test-framework :refer :all]
   [jinteki.cards :refer [all-cards]]
   [jinteki.utils :refer [add-cost-to-label]]))

(deftest combine-abilities
  (testing "Combining 2 abilities"
    (do-game
      (new-game {:corp {:deck ["Enigma"]}})
      (play-from-hand state :corp "Enigma" "HQ")
      (rez state :corp (get-ice state :hq 0))
      (let [cr (:credit (get-corp))]
        (core/resolve-ability state :corp (eid/make-eid state)
                              (core/combine-abilities (ice/gain-credits-sub 1)
                                                      (ice/gain-credits-sub 2))
                              (get-ice state :hq 0) nil)
        (is (= (+ 3 cr) (:credit (get-corp))) "Corp gained 3 credits"))))
  (testing "Combining 3 abilities"
    (do-game
      (new-game {:corp {:deck ["Enigma"]}})
      (play-from-hand state :corp "Enigma" "HQ")
      (rez state :corp (get-ice state :hq 0))
      (let [cr (:credit (get-corp))]
        (core/resolve-ability state :corp (eid/make-eid state)
                              (core/combine-abilities (ice/gain-credits-sub 1)
                                                      (ice/gain-credits-sub 2)
                                                      (ice/gain-credits-sub 3))
                              (get-ice state :hq 0) nil)
        (is (= (+ 6 cr) (:credit (get-corp))) "Corp gained 6 credits"))))
  (testing "Combining trace abilities"
    (do-game
      (new-game {:corp {:deck ["Enigma"]}})
      (play-from-hand state :corp "Enigma" "HQ")
      (rez state :corp (get-ice state :hq 0))
      (let [cr (:credit (get-corp))]
        (core/resolve-ability state :corp (eid/make-eid state)
                              (core/combine-abilities (ice/trace-ability 1 (ice/gain-credits-sub 1))
                                                      (ice/trace-ability 2 (ice/gain-credits-sub 2))
                                                      (ice/trace-ability 3 (ice/gain-credits-sub 3)))
                              (get-ice state :hq 0) nil)
        (click-prompt state :corp "0")
        (click-prompt state :runner "0")
        (is (= (+ 1 cr) (:credit (get-corp))) "Corp gained 1 credit")
        (click-prompt state :corp "0")
        (click-prompt state :runner "0")
        (is (= (+ 3 cr) (:credit (get-corp))) "Corp gained 2 credits")
        (click-prompt state :corp "0")
        (click-prompt state :runner "0")
        (is (= (+ 6 cr) (:credit (get-corp))) "Corp gained 3 credits")))))

(deftest trash-icon
  (doseq [card (->> (vals @all-cards)
                    (filter #(re-find #"(?i)\[trash\].*:" (:text % ""))))
          :when (not-empty (card-def card))]
    (is (core/has-trash-ability? card)
        (str (:title card) " needs either :cost [(->c :trash-can)] or :trash-icon true"))))

(deftest actions-are-documented
  "This ensures that every ability with a (->c :click ...) cost is marked as either :action true, or :action (otherwise)
   this is important for the functionality of undo-click, so it's worth ensuring"
  (doseq [card (vals @all-cards)
          :let [cdef (card-def card)]
          :when (not-empty cdef)
          {:keys [cost] :as ab} (apply concat (vals (select-keys (card-def card) [:abilities :corp-abilities :runner-abilities])))
          :when (and cost (or (and (sequential? cost)
                                   (some #(= (:cost/type %) :click) cost))
                              (= (:cost/type cost) :click)))]
    (is (contains? ab :action) (str (:title card) " may have unlabelled actions (use :action true) or (:action false/nil)"))))

(defn- x-has-labels
  [x-key x-name]
  (doseq [[title cards-with-x]
          (->> @all-cards
               vals
               (sort-by (juxt :type :title))
               (map (juxt :title card-def))
               (filter (comp x-key second)))
          [idx x] (map-indexed (fn [idx itm] [idx itm]) (x-key cards-with-x))]
    (is (string? (or (:label x) (:msg x))) (str title ": " x-name " " (inc idx) " doen't have an appropriate label"))))

(deftest abilities-have-labels
  (x-has-labels :abilities "Ability")
  (x-has-labels :corp-abilities "Runner Ability")
  (x-has-labels :runner-abilities "Corp Ability")
  (x-has-labels :subroutines "Subroutine"))

(defn generate-label
  [card]
  (add-cost-to-label (first (:abilities card))))

(deftest cost-label
  (testing "Conditional costs"
    (do-game
      (new-game {:runner {:hand ["Simulchip" "Gorman Drip v1"]
                          :credits 10}})
      (take-credits state :corp)
      (play-from-hand state :runner "Simulchip")
      (is (= "[trash], trash 1 installed program: Install a program from the heap"
             (generate-label (get-hardware state 0))))
      (play-from-hand state :runner "Gorman Drip v1")
      (card-ability state :runner (get-program state 0) 0)
      (is (= "[trash]: Install a program from the heap"
             (generate-label (get-hardware state 0))))))
  (testing "trash icon"
    (do-game
      (new-game {:runner {:hand ["Bankroll"]}})
      (take-credits state :corp)
      (play-from-hand state :runner "Bankroll")
      (is (= "[trash]: Take all hosted credits" (generate-label (get-program state 0))))))
  (testing "x credits"
    (do-game
      (new-game {:runner {:hand ["Misdirection"]}})
      (take-credits state :corp)
      (play-from-hand state :runner "Misdirection")
      (is (= "[Click][Click], X [Credits]: Remove X tags" (generate-label (get-program state 0))))))
  (testing "x power counters"
    (do-game
      (new-game {:corp {:hand ["Lakshmi Smartfabrics"]}})
      (play-from-hand state :corp "Lakshmi Smartfabrics" "New remote")
      (let [lak (get-content state :remote1 0)]
        (rez state :corp lak)
        (is (= "X hosted power counters: Reveal an agenda worth X points from HQ"
               (generate-label (refresh lak))))))))
