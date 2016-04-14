package pt.ist.cnv.cloudprime;

import java.math.BigInteger;
import java.util.ArrayList;


public class IntegerFactoring {

    private final BigInteger zero = new BigInteger("0");
    private final BigInteger one = new BigInteger("1");
    
    private ArrayList<BigInteger> calcPrimeFactors(BigInteger num, ArrayList<BigInteger> factors) {
        
        BigInteger divisor = new BigInteger("2");

        if (num.compareTo(one) == 0) {
            return factors;
        }
        
        while (num.remainder(divisor).compareTo(zero) != 0) {
            divisor = divisor.add(one);
        }
        
        factors.add(divisor);
        
        return calcPrimeFactors(num.divide(divisor), factors);
    }

    public String run(String numberToFactor){

        ArrayList<BigInteger> factors = calcPrimeFactors(new BigInteger(numberToFactor), new ArrayList<BigInteger>());
        String result ="";

        for(int i= 0; i < factors.size(); i++){

            BigInteger bi = factors.get(i);
            result += bi.toString();
            if (i != (factors.size() -1)) {
                result += " x ";
            }
        }

        return result;
    }

    public static String main(String arg){
        return new IntegerFactoring().run(arg);
    }
}


