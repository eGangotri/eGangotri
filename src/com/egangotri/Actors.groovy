package com.egangotri

import groovy.util.logging.Slf4j

import static groovyx.gpars.actor.Actors.*

@Slf4j
class Actors {

    def actor = actor {
        loop {
            react {
                log.info(it)
            }
        }
    }

    def test(){
        actor << 'Message-1'
        actor.send 'Message-22'
        def reply1 = actor.sendAndWait('Message-32')
        log.info("Repy is $reply1")
    }

    static void main(def args){
       // new Actors().test()
        List ZIP = [".zip", ".rar"]
        log.info(ZIP.find{"ram.rar".endsWith(it.toString())})
        log.info( ZIP.find{"rar.ram".endsWith(it.toString())})
        log.info(ZIP.find{"ram.rar2".endsWith(it.toString())})


    }
}

