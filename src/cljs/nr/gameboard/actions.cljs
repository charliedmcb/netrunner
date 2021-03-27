(ns nr.gameboard.actions
  (:require [differ.core :as differ]
            [nr.appstate :refer [app-state]]
            [nr.gameboard.log :refer [log-mode]]
            [nr.gameboard.replay :refer [init-replay]]
            [nr.gameboard.state :refer [game-state last-state lock check-lock? parse-state get-side not-spectator?]]
            [nr.translations :refer [tr]]
            [nr.utils :refer [toastr-options]]
            [nr.ws :as ws]))

(defn init-game [state]
  (let [side (get-side state)]
    (.setItem js/localStorage "gameid" (:gameid @app-state))
    (reset! log-mode :log)
    (reset! game-state (dissoc state :replay-diffs :replay-jump-to))
    (swap! game-state assoc :side side)
    (reset! last-state @game-state)
    (reset! lock false)
    (when (:replay-diffs state)
      (init-replay app-state state))))

(defn launch-game [state]
  (init-game state)
  (set! (.-onbeforeunload js/window) #(clj->js "Leaving this page will disconnect you from the game."))
  (-> "#gamelobby" js/$ .fadeOut)
  (-> "#gameboard" js/$ .fadeIn))
(defn handle-diff [{:keys [gameid diff]}]
  (when (= gameid (:gameid @game-state))
    (swap! game-state #(differ/patch @last-state diff))
    (check-lock?)
    (reset! last-state @game-state)))

(declare toast)
(defn handle-timeout [{:keys [gameid]}]
  (when (= gameid (:gameid @game-state))
    (toast (tr [:game.inactivity "Game closed due to inactivity"]) "error" {:time-out 0 :close-button true})))

(ws/register-ws-handler! :netrunner/state #(init-game (parse-state %)))
(ws/register-ws-handler! :netrunner/start #(launch-game (parse-state %)))
(ws/register-ws-handler! :netrunner/diff #(handle-diff (parse-state %)))
(ws/register-ws-handler! :netrunner/timeout #(handle-timeout (parse-state %)))

(defn send-command
  ([command] (send-command command nil))
  ([command {:keys [no-lock card] :as args}]
   (when (and (not (:replay @game-state))
              (or (not @lock) no-lock))
     (let [card (select-keys card [:cid :zone :side :host :type])
           args (merge args (when (seq card) {:card card}))]
       (try (js/ga "send" "event" "game" command) (catch js/Error e))
       (when-not no-lock (reset! lock true))
       (ws/ws-send! [:netrunner/action {:gameid-str (:gameid @game-state)
                                        :command command
                                        :args args}])))))

(defn mute-spectators [mute-state]
  (when (not (:replay @game-state))
    (ws/ws-send! [:netrunner/mute-spectators {:gameid-str (:gameid @game-state)
                                              :mute-state mute-state}])))

(defn stack-servers []
  (swap! app-state update-in [:options :stacked-servers] not))

(defn flip-runner-board []
  (let [layout (if (= "irl" (get-in @app-state [:options :runner-board-order])) "jnet" "irl")]
    (swap! app-state assoc-in [:options :runner-board-order] layout)))

(defn concede []
  (when (not (:replay @game-state))
    (ws/ws-send! [:netrunner/concede {:gameid-str (:gameid @game-state)}])))

(defn toast
  "Display a toast warning with the specified message.
  Sends a command to clear any server side toasts."
  [msg type options]
  (set! (.-options js/toastr) (toastr-options options))
  (let [f (aget js/toastr (if (= "exception" type) "error" type))]
    (f (if (= "exception" type) (build-exception-msg msg (:last-error @game-state)) msg))
    (send-command "toast")))
