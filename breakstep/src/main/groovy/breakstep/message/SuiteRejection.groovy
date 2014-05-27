package breakstep.message

class SuiteRejection implements Serializable {
    private static final long serialVersionUID = 1L
    String suiteName

    SuiteRejection(String suiteName) {
        this.suiteName = suiteName
    }
}
