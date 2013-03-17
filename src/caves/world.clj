(ns caves.world)

; Constants
(def world-size [160 50])

; Data structures
(defrecord World [tiles])
(defrecord Tile [kind glyph color])

(def tiles
  {:floor (new Tile :floor  "." :white)
   :wall  (new Tile :wall   "#" :white)
   :bound (new Tile :bound  "X" :black)})

; The get-in function takes a map and a vector
; of keys. It returns the value of the key if it
; is found, or (:bound tiles) otherwise.
(defn get-tile
  [tiles x y]
  (get-in tiles [y x] (:bound tiles)))

; letfn is like let but for functions
; rand-nth returns a random element of the collection
; vec creates a new vectors containing the contents of the collection
(defn random-tiles
  []
  (let [[cols rows] world-size]
    (letfn [(random-tile
              []
              (tiles (rand-nth [:floor :wall])))
            (random-row
              []
              (vec (repeatedly cols random-tile)))]
      (vec (repeatedly rows random-row)))))

(defn random-world
  []
  (new World (random-tiles)))
(ns caves.world)

; Constants
(def world-size [160 50])

; Data structures
(defrecord World [tiles])
(defrecord Tile [kind glyph color])

(def tiles
  {:floor (new Tile :floor  "." :white)
   :wall  (new Tile :wall   "#" :white)
   :bound (new Tile :bound  "X" :black)})

; The get-in function takes a map and a vector
; of keys. It returns the value of the key if it
; is found, or (:bound tiles) otherwise.
(defn get-tile
  [tiles x y]
  (get-in tiles [y x] (:bound tiles)))

; letfn is like let but for functions
; rand-nth returns a random element of the collection
; vec creates a new vectors containing the contents of the collection
(defn random-tiles
  []
  (let [[cols rows] world-size]
    (letfn [(random-tile
              []
              (tiles (rand-nth [:floor :wall])))
            (random-row
              []
              (vec (repeatedly cols random-tile)))]
      (vec (repeatedly rows random-row)))))

(defn random-world
  []
  (new World (random-tiles)))
