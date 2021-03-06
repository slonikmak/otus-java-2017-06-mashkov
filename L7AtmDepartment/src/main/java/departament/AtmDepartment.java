package departament;


import ru.otus.interfaces.ATMmashine;
import ru.otus.interfaces.Strategy;

import java.util.ArrayList;
import java.util.Optional;

public class AtmDepartment implements ATMmashine {
    private ArrayList<ATMmashine> atMmashines;

    public AtmDepartment(){
        atMmashines = new ArrayList<ATMmashine>();
    }

    public void addATM(ATMmashine atm){
        atMmashines.add(atm);
    }

    public long getBalance() {
        return atMmashines.stream().map(ATMmashine::getBalance).reduce((b1, b2)->b1+b2).get();
    }

    public void setDefaultMoney() {
        atMmashines.forEach(ATMmashine::setDefaultMoney);
    }

    public void getMoney(long summ) throws Exception {
        throw new Exception("Метод не работает, обратитесь в банкомат");
    }

    public void setStrategy(Strategy strategy) {
        atMmashines.forEach(atm->atm.setStrategy(strategy));
    }

    public void getMoney(long summ, int numOfAtm) throws Exception {
        if (numOfAtm>=atMmashines.size()) return;
        atMmashines.get(numOfAtm).getMoney(summ);
    }

    public Optional<Object> getBalance(int numOfAtm){
        if (numOfAtm>=atMmashines.size()) return Optional.empty();
        return Optional.of(atMmashines.get(numOfAtm).getBalance());
    }
}
