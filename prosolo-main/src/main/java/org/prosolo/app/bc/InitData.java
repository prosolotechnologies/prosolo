package org.prosolo.app.bc;

import org.prosolo.app.bc.test.*;

/**
 * @author stefanvuckovic
 * @date 2019-05-20
 * @since 1.3.2
 */
public enum InitData {

    DEMO(false) {
        @Override
        public BusinessCase getDataInitializer() {
            return new BusinessCase5_Demo();
        }
    },
    TUTORIAL(false) {
        @Override
        public BusinessCase getDataInitializer() {
            return new BusinessCase5_Tutorial();
        }
    },
    TEST_1_1(true) {
        @Override
        public BusinessCase getDataInitializer() {
            return new BusinessCase_Test_1_1();
        }
    },
    TEST_2_1(true) {
        @Override
        public BusinessCase getDataInitializer() {
            return new BusinessCase_Test_2_1();
        }
    },
    TEST_2_3(true) {
        @Override
        public BusinessCase getDataInitializer() {
            return new BusinessCase_Test_2_3();
        }
    },
    TEST_2_6(true) {
        @Override
        public BusinessCase getDataInitializer() {
            return new BusinessCase_Test_2_6();
        }
    },
    TEST_2_8(true) {
        @Override
        public BusinessCase getDataInitializer() {
            return new BusinessCase_Test_2_8();
        }
    },
    TEST_2_9(true) {
        @Override
        public BusinessCase getDataInitializer() {
            return new BusinessCase_Test_2_9();
        }
    },
    TEST_2_10(true) {
        @Override
        public BusinessCase getDataInitializer() {
            return new BusinessCase_Test_2_10();
        }
    },
    TEST_2_11(true) {
        @Override
        public BusinessCase getDataInitializer() {
            return new BusinessCase_Test_2_11();
        }
    },
    TEST_2_12(true) {
        @Override
        public BusinessCase getDataInitializer() {
            return new BusinessCase_Test_2_12();
        }
    },
    TEST_2_13(true) {
        @Override
        public BusinessCase getDataInitializer() {
            return new BusinessCase_Test_2_13();
        }
    },
    TEST_2_14(true) {
        @Override
        public BusinessCase getDataInitializer() {
            return new BusinessCase_Test_2_14();
        }
    },
    TEST_2_15(true) {
        @Override
        public BusinessCase getDataInitializer() {
            return new BusinessCase_Test_2_15();
        }
    },
    TEST_3_1(true) {
        @Override
        public BusinessCase getDataInitializer() {
            return new BusinessCase_Test_3_1();
        }
    },
    TEST_3_2(true) {
        @Override
        public BusinessCase getDataInitializer() {
            return new BusinessCase_Test_3_2();
        }
    },
    TEST_3_3(true) {
        @Override
        public BusinessCase getDataInitializer() {
            return new BusinessCase_Test_3_3();
        }
    },
    TEST_3_4(true) {
        @Override
        public BusinessCase getDataInitializer() {
            return new BusinessCase_Test_3_4();
        }
    };

    private boolean test;

    InitData(boolean test) {
        this.test = test;
    }

    public boolean isTest() {
        return test;
    }

    public abstract BusinessCase getDataInitializer();
}
