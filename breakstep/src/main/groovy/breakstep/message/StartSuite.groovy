package breakstep.message

class StartSuite implements Serializable {
    private static final long serialVersionUID = 1L
    def final suiteName

    StartSuite(String suite){
        suiteName = suite
    }
}
