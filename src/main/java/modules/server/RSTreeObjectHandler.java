package modules.server;


import modules.testExecutor.templates.RSTests;

import java.util.concurrent.CopyOnWriteArrayList;

public class RSTreeObjectHandler {
    private RSTests object = new RSTests();
    private RSTests newObject = new RSTests();
    private String[] pathArray;

    public RSTreeObjectHandler(RSTests object, RSTests newObject, String path) {
        this.object = object;
        this.newObject = newObject;
        this.pathArray = path.split("\\.");
    }

    public void setObject(RSTests object) {
        this.object = object;
    }

    public void setNewObject(RSTests newObject) {
        this.newObject = newObject;
    }

    public void setPath(String path) {
        this.pathArray = path.split("\\.");
    }

    public void operation(String operation) {
        int listId, countPathId = 0;
        boolean press;

        if(pathArray[0].length() == 0 && object.getList().size() == 0) {
            CopyOnWriteArrayList<RSTests> newRSList = new CopyOnWriteArrayList<>();
            newRSList.add(newObject);
            object.setList(newRSList);
        } else {
            for (String pathId : pathArray) {
                listId = 0;
                CopyOnWriteArrayList<RSTests> rsList = new CopyOnWriteArrayList<>();
                press = false;
                for (RSTests subRSTest : object.getList()) {
                    if (Integer.parseInt(pathId) == listId) { //номер элемента массива соответсвует пути pathId
                        if (pathArray.length > 0 && countPathId == pathArray.length - 1 && listId == Integer.parseInt(pathArray[countPathId])) { // текущий лист - целевой
                            switch (operation) {
                                case "update":
                                    rsList.add(newObject);
                                    break;
                                case "add":
                                    rsList.add(subRSTest);
                                    rsList.add(newObject);
                                    break;
                                case "addSub":
                                    CopyOnWriteArrayList<RSTests> rsListSub = new CopyOnWriteArrayList<>();
                                    if (subRSTest.getList() != null) rsListSub.addAll(subRSTest.getList());
                                    rsListSub.add(newObject);
                                    subRSTest.setList(rsListSub);
                                    rsList.add(subRSTest);
                                    break;
                                case "remove":
                                    break;
                            }
                            press = true;

                        } else {
                            if (!press) {
                                object = subRSTest;
                                break;
                            } else {
                                rsList.add(subRSTest);
                            }
                        }

                    } else {
                        rsList.add(subRSTest);
                    }
                    listId++;
                    if (press) {
                        object.setList(rsList);
                    }
                }

                countPathId++;
            }
        }
    }
}
