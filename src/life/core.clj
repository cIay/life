(ns life.core
    (:gen-class)
    (:use [life.game-io])
    (:import (javax.swing JFrame JPanel)))

(defn init-game []
    (let [frame (JFrame. "life")
          panel (JPanel.)]
        (.setDefaultCloseOperation frame JFrame/EXIT_ON_CLOSE)
        (.setSize frame (:w frame-size) (:h frame-size))
        (.setResizable frame false)
        (.setContentPane frame panel)
        (.setBackground panel background-colour)
        (.setVisible frame true)

        (let [gfx (.getGraphics panel)]
            (.addKeyListener frame (create-ka gfx))
            (.addMouseMotionListener panel (create-mma gfx))
            (.addMouseListener panel (create-ma gfx))
            (.addMouseWheelListener panel (create-mwl gfx))
            (game-loop gfx))))

(defn -main [& args]
    (init-game))