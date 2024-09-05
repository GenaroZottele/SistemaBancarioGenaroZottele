//package ar.edu.utn.frbb.tup.persistence;
//
//import ar.edu.utn.frbb.tup.persistence.entity.TransferEntity;
//import org.springframework.stereotype.Service;
//
//import java.util.List;
//
//@Service
//public class TransferDao extends AbstractBaseDao {
//
//    private static final String FILENAME = "transfers.txt";
//
//    public void save(TransferEntity transfer) {
//        getInMemoryDatabase().put(transfer.getId(), transfer);
//        saveToFile(FILENAME, transfer.toString());
//    }
//
//    public TransferEntity find(long id) {
//        if (getInMemoryDatabase().get(id) == null) {
//            return null;
//        }
//        return (TransferEntity) getInMemoryDatabase().get(id);
//    }
//
//    public List<String> findAll() {
//        return readFromFile(FILENAME);
//    }
//
//    @Override
//    protected String getEntityName() {
//        return "TRANSFER";
//    }
//
//    @Override
//    protected String objectToString(Object object) {
//        return "";
//    }
//
//    @Override
//    protected Object stringToObject(String line) {
//        return null;
//    }
//}