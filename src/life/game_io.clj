(ns life.game-io
    (:use life.game-rules)
    (:require clojure.set)
    (:import (javax.swing SwingUtilities)
             (java.awt Graphics Color)
             (java.awt.event MouseAdapter MouseMotionAdapter MouseEvent 
                             MouseWheelListener MouseWheelEvent 
                             KeyAdapter KeyEvent)))

(defrecord FrameSize [w h])
(defrecord CellSize [w h])

(def frame-size (FrameSize. 640 480))
(def cell-size (atom (CellSize. 8 8)))

(def alive-colour (Color/BLACK))
(def dead-colour (Color/LIGHT_GRAY))
(def background-colour (Color/LIGHT_GRAY))

(def pause (atom nil))
(def speed (atom 200))
;(def state (atom #{[0 2] [1 3] [2 1] [2 2] [2 3]})) ;glider
(def state (atom #{}))
(def state-lock (atom nil))


(defn draw-cells [gfx cells colour]
    (locking gfx
        (.setColor gfx colour)
        (doseq [cell cells] 
            (let [x (* (:w @cell-size) (get cell 0)) 
                  y (* (:h @cell-size) (get cell 1))]
                (.fillRect gfx x y (:w @cell-size) (:h @cell-size))))))

(defn game-loop [gfx]
    (when (nil? @pause)
        (loop []
            (if (compare-and-set! state-lock nil :on)
                (let [prev-state @state]
                    (swap! state life-tick)
                    (draw-cells gfx @state alive-colour)
                    (draw-cells gfx (clojure.set/difference prev-state @state) dead-colour)
                    (reset! state-lock nil))
                (recur))))
    (Thread/sleep @speed)
    (recur gfx))

(defn coords-to-cell [x y]
    #{[(quot x (:w @cell-size))
       (quot y (:h @cell-size))]})

(defn add-cell [gfx me]
    (let [new-cell (coords-to-cell (.getX me) (.getY me))]
        (loop []
            (if (compare-and-set! state-lock nil :on) 
                (do (draw-cells gfx new-cell alive-colour) 
                    (swap! state clojure.set/union new-cell)
                    (reset! state-lock nil))
                (recur)))))

(defn del-cell [gfx me]
    (let [old-cell (coords-to-cell (.getX me) (.getY me))]
        (draw-cells gfx old-cell dead-colour)
        (swap! state clojure.set/difference old-cell)))

(defn handle-mouse-event [gfx me]
    (if (SwingUtilities/isLeftMouseButton me) (add-cell gfx me))
    (if (SwingUtilities/isRightMouseButton me) (del-cell gfx me)))

(defn ^MouseMotionAdapter create-mma [gfx]
    (proxy [MouseMotionAdapter] []
        (mouseDragged [^MouseEvent me]
            (handle-mouse-event gfx me))))

(defn ^MouseAdapter create-ma [gfx]
    (proxy [MouseAdapter] []
        (mouseClicked [^MouseEvent me]
            (handle-mouse-event gfx me))))

(defn clear-screen [gfx]
    (locking gfx
        (.setColor gfx background-colour)
        (.fillRect gfx 0 0 (:w frame-size) (:h frame-size))))

(defn resize [gfx mwe]
    (let [rotation (- (.getWheelRotation mwe))
          new-width (+ (:w @cell-size) rotation)
          new-height (+ (:h @cell-size) rotation)]
        (when (or (> new-width 0) (> new-height 0))
            (clear-screen gfx)
            (reset! cell-size (CellSize. new-width new-height))
            (draw-cells gfx @state alive-colour))))

(defn ^MouseWheelListener create-mwl [gfx]
    (proxy [MouseWheelListener] []
        (mouseWheelMoved [^MouseWheelEvent mwe]
            (resize gfx mwe))))


(defn ^KeyAdapter create-ka [gfx]
    (proxy [KeyAdapter] []
        (keyPressed [^KeyEvent ke]
            (let [code (.getKeyCode ke)]
                (condp = code
                    (KeyEvent/VK_SPACE) (if @pause (reset! pause nil) (reset! pause :on))
                    (KeyEvent/VK_RIGHT) (if (> @speed 0) (swap! speed - 1)) 
                    (KeyEvent/VK_LEFT) (swap! speed + 1)
                    (KeyEvent/VK_ENTER) (do (swap! state empty) (clear-screen gfx))
                    nil)))))