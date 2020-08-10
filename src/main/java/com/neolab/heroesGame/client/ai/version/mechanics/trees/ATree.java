package com.neolab.heroesGame.client.ai.version.mechanics.trees;

import com.neolab.heroesGame.client.ai.version.mechanics.arena.Answer;
import com.neolab.heroesGame.client.ai.version.mechanics.nodes.ANode;

import java.util.Objects;

public abstract class ATree {
    private final ANode root;
    private ANode currentNode;

    protected ATree(final ANode root, final ANode currentNode) {
        this.root = root;
        this.currentNode = currentNode;
    }

    public ANode getRoot() {
        return root;
    }

    public ANode getCurrentNode() {
        return currentNode;
    }

    public void setCurrentNode(final ANode currentNode) {
        this.currentNode = currentNode;
    }

    public boolean upToParent() {
        if (currentNode != root) {
            currentNode = currentNode.getParent();
            return true;
        }
        return false;
    }

    public boolean downToChild(final int index) {
        final ANode temp = currentNode.getChild(index);
        if (temp != null) {
            currentNode = temp;
            return true;
        }
        return false;
    }

    public boolean downToChild(final ANode node) {
        if (node != null && currentNode.equals(node.getParent())) {
            currentNode = node;
            return true;
        }
        //Никогда не должно происходить. Если произошло, значит есть ошибка в программе
        throw new AssertionError();
    }

    public boolean isNodeNew() {
        return currentNode.isNewNode();
    }

    public void toRoot() {
        currentNode = root;
    }

    public Answer getAnswer(final int actionNumber) {
        return currentNode.getChild(actionNumber).getPrevAnswer();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ATree aTree = (ATree) o;
        return Objects.equals(root, aTree.root) &&
                Objects.equals(currentNode, aTree.currentNode);
    }

    @Override
    public int hashCode() {
        return Objects.hash(root, currentNode);
    }
}
