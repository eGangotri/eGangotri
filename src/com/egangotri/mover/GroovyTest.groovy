package com.egangotri.mover

/**
 * Created by user on 6/6/2017.
 */
class GroovyTest {
    static main(args) {


        List value = [1, 4, 6, 8, 3, 6, 3]
        int n = 2
        print (findLargestN(value, 4))
        assert value == [1,4,6,8,3,6,3]

    }
    def static findLargestN( List values, int n) {
        if(values && n > 0){
            List x = values.clone()
            values.sort(false).reverse().take(n)
        }
        else{
            return []
        }

    }
}


