package backend.academy.scrapper.dto;

import java.util.List;

public class StackOverflowResponse {
    private List<StackOverflowQuestion> items;

    public List<StackOverflowQuestion> getItems() {
        return items;
    }

    public void setItems(List<StackOverflowQuestion> items) {
        this.items = items;
    }
}
