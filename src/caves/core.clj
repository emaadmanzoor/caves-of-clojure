(ns caves.core
  (:use [caves.world :only [random-world]])
  (:require [lanterna.screen :as s]))

; Constants
(def screen-size [80 24])

; Data structures
(defrecord UI [kind])
(defrecord World [])
(defrecord Game [world uis input])

; Utility functions
(defn clear-screen [screen]
  (let [[cols rows] screen-size
        blank (apply str (repeat cols \space))]
    ; Define blank to be a string of 80 spaces
    (doseq [row (range rows)]
      ; Basically a for-loop from 0-23 (range 24)
      ; Put the 80 spaces at column 0, row <row>
      ; Assumes the screen is 80 columns x 24 rows
      (s/put-string screen 0 row blank))))

; Below we use multimethods. These are described
; nicely here: http://www.fatvat.co.uk/2009/01/multi-methods-in-clojure.html
; They are composed of a dispatcher, which is a test condition on the
; input that selects which method to dispatch, and a bunch of methods
; which react to the output of the dispatcher.

(defmulti draw-ui
  (fn [ui game screen]
    (:kind ui)))

; This multimethod takes in three arguments: ui, game and screen.
; The dispatcher functions returns the kind of ui (ui.kind, for Java'ers)
; Each of the methods functions with a different :kind of ui

(defmethod draw-ui :start [ui game screen]
  ; Method to work with the :start :kind of ui
  (s/put-string screen 0 0 "Welcome to the Caves")
  (s/put-string screen 0 1 "Press enter to win, anything else to lose"))

(defmethod draw-ui :win [ui game screen]
  (s/put-string screen 0 0 "You win!")
  (s/put-string screen 0 1 "Press any key to continue..."))

(defmethod draw-ui :lose [ui game screen]
  (s/put-string screen 0 0 "You lose!")
  (s/put-string screen 0 1 "Press any key to continue..."))

(defn draw-game [game screen]
  (clear-screen screen)
  (doseq [ui (:uis game)]
    ; for (UI ui : game.uis) { /* Do something to ui */ }
    (draw-ui ui game screen))
  (s/redraw screen))

; This multimethod processes input for the :kind of ui
; that is at the top of the :uis stack of the game

(defmulti process-input
  (fn [game input]
    (:kind (last (:uis game)))))

; assoc modifies a map or a vector. In the code
; below, game is the "map", :uis is the key
; which is given a new assignment. The modified
; game map is returned.

(defmethod process-input :start [game input]
  (if (= input :enter)
    (assoc game :uis [(new UI :win)])
    (assoc game :uis [(new UI :lose)])))

(defmethod process-input :win [game input]
  (if (= input :escape)
    (assoc game :uis [])
    (assoc game :uis [(new UI :start)])))

(defmethod process-input :lose [game input]
  (if (= input :escape)
    (assoc game :uis [])
    (assoc game :uis [(new UI :start)])))

(defmethod process-input :play [game input]
  (case input
    :enter      (assoc game :uis [(new UI :win)])
    :backspace  (assoc game :uis [(new UI :lose)])
    game))

(defn get-input [game screen]
  (assoc game :input (s/get-key-blocking screen)))

; This is an example of destructuring in Clojure.
; game is a record with fields :world, :input,
; and :uis. We want to bind the :input and :uis
; to variables while loop-recur'ing over the game
; record itself (so we bind that to "game" with
; the ":as game" code.) 
; 
; We could have done:
;   [{the-input :input the-uis :uis :as game} game]
; but we want the loop variables to have the
; same names as the map variables. A shortcut to
; do this is to use :keys [input uis] as below.
;
; So this entire statement:
;   loop [{:keys [input uis] :as game} game
; has two purposes:
;   1. Extract the input and uis into local variables
;      with the same name as the map keys
;   2. Store the original map into a local variable
;
; Nice post on destructuring: http://blog.jayfields.com/2010/07/clojure-destructuring.html
;
; dissoc removes the value for a key from the map

(defn run-game [game screen]
  (loop [{:keys [input uis] :as game} game]
    (when-not (empty? uis)
      (draw-game game screen)
      (if (nil? input)
        (recur (get-input game screen))
        (recur (process-input (dissoc game :input) input))))))

(defn new-game []
  (new Game
       (new World)
       [(new UI :start)]
       nil))

; Look up what "future" is in Clojure

(defn main
  ([screen-type] (main screen-type false))
  ([screen-type block?]
   (letfn [(go []
             (let [screen (s/get-screen screen-type)]
               (s/in-screen screen
                            (run-game (new-game) screen))))]
     (if block?
       (go)
       (future (go))))))

(defn -main [& args]
  (let [args (set args)
        screen-type (cond
                      (args ":swing") :swing
                      (args ":text")  :text
                      :else           :text)]
    (main screen-type true)))
