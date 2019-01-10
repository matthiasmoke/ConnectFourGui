import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Represents a group of checkers in the game-board
 */
public class Group implements Cloneable {

    private GroupType type;
    private List<Checker> members = new ArrayList<>(4);

    /**
     * Initializes a group with members and certain type.
     *
     * @param members Members of the group.
     * @param type Type members of the group are arranged.
     */
    public Group(Collection<Checker> members, GroupType type) {
        addMembers(members);
        this.type = type;
    }

    /**
     * Gets members of the group.
     *
     * @return members of the group.
     */
    public List<Checker> getMembers() {
        return members;
    }

    /**
     * Gets the owner of the group
     *
     * @return Owner of the group.
     */
    public Player getOwner() {
        return members.get(0).getOwner();
    }

    /**
     * Gets type of the group.
     *
     * @return type of the group.
     */
    public GroupType getType() {
        return type;
    }

    /**
     * Checks if group has a certain member.
     *
     * @param member Member that has to be searched for.
     * @return true if given member is part of the group.
     */
    public boolean hasMember(Checker member) {
        int col = member.getPosition().getColumn();
        int row = member.getPosition().getRow();

        for (Checker checker : members) {
            if (col == checker.getPosition().getColumn()
                    && row == checker.getPosition().getRow()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Adds members to the group.
     *
     * @param memberList List of members to add.
     */
    public void addMembers(Collection<Checker> memberList) {
        for (Checker member : memberList) {

            // If group size smaller than 4 and member isn't already member.
            if (members.size() < Board.CONNECT && !hasMember(member)) {
                members.add(member);
            }
        }
    }

    /**
     * Gets members of the group in sorted order by using insertion-sort
     *
     * @return Collection of members in sorted order.
     */
    public Collection<Checker> getSortedMembers() {
        int key;

        for (int i = 1; i < members.size(); i++) {
            key = getKeyForSort(i);
            Checker save = members.get(i);
            int j = i - 1;

            while (j >= 0 && getKeyForSort(j) > key) {
                members.set(j + 1, members.get(j));
                j--;
            }

            members.set(j + 1, save);
        }
        return members;

    }

    /**
     * Gets the right key for sort by using group type.
     * For vertical groups it returns the row index, for others, the column
     *
     * @param index Index of the member in members.
     * @return Key value of member on index.
     */
    private int getKeyForSort(int index) {
        int key;

        if (type == GroupType.VERTICAL) {
            key = members.get(index).getPosition().getRow();
        } else {
            key = members.get(index).getPosition().getColumn();
        }

        return key;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Group clone() {
        Group copy;
        List<Checker> membersCopy =  new ArrayList<>(4);

        try {
            copy = (Group) super.clone();
        } catch (CloneNotSupportedException ex) {
            throw new Error(ex);
        }

        for (Checker member : members) {
            membersCopy.add(member);
        }

        copy.members = membersCopy;
        return copy;
    }
}
