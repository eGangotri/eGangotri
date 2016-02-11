package com.egangotri

import static groovyx.gpars.actor.Actors.*

class Actors {

    def actor = actor {
        loop {
            react {
                println it
            }
        }
    }

    def test(){
        actor << 'Message-1'
        actor.send 'Message-22'
        def reply1 = actor.sendAndWait('Message-32')
        println "Repy is $reply1"
    }

    public static void main(def args){
       // new Actors().test()
        List ZIP = [".zip", ".rar"]
        println ZIP.find{"ram.rar".endsWith(it.toString())}
        println ZIP.find{"rar.ram".endsWith(it.toString())}
        println ZIP.find{"ram.rar2".endsWith(it.toString())}


    }
}

