/*
 * Copyright 2013 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package litresbot.books.convert;

import java.util.Iterator;
import java.util.NoSuchElementException;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class NodeListIterator implements Iterator<Node> {

  /** the original NodeList instance */
  private final NodeList nodeList;
  /** The current iterator index */
  private int index = 0;

  /**
   * Convenience constructor, which creates a new NodeListIterator from
   * the specified node's childNodes.
   *
   * @param node Node, who's child nodes are wrapped by this class. Must not be null
   * @throws NullPointerException if node is null
   */
  public NodeListIterator(final Node node) {
      if (node == null) {
          throw new NullPointerException("Node must not be null.");
      }
      this.nodeList = node.getChildNodes();
  }

  /**
   * Constructor, that creates a new NodeListIterator from the specified
   * <code>org.w3c.NodeList</code>
   *
   * @param nodeList node list, which is wrapped by this class. Must not be null
   * @throws NullPointerException if nodeList is null
   */
  public NodeListIterator(final NodeList nodeList) {
      if (nodeList == null) {
          throw new NullPointerException("NodeList must not be null.");
      }
      this.nodeList = nodeList;
  }

  public Iterable<Node> getIterable() {
      return new Iterable<Node>() {
          @Override
          public Iterator<Node> iterator() {
              return new NodeListIterator(nodeList);
          }
      };
  }

  @Override
  public boolean hasNext() {
      return nodeList != null && index < nodeList.getLength();
  }

  @Override
  public Node next() {
      if (nodeList != null && index < nodeList.getLength()) {
          return nodeList.item(index++);
      }
      throw new NoSuchElementException("underlying nodeList has no more elements");
  }

  public boolean hasPrevious() {
      return nodeList != null && index > 0;
  }

  public Node previous() {
      if (nodeList != null && index > 0) {
          return nodeList.item(index--);
      }
      throw new NoSuchElementException("underlying nodeList has no more elements");
  }

  public void setEnd() {
    if (nodeList == null) throw new NullPointerException("NodeList must not be null.");
    if (nodeList.getLength() == 0) {
      index = 0;
      return;
    }
    index = nodeList.getLength() - 1;
}

  /**
   * Throws {@link UnsupportedOperationException}.
   *
   * @throws UnsupportedOperationException always
   */
  @Override
  public void remove() {
      throw new UnsupportedOperationException("remove() method not supported for a NodeListIterator.");
  }
}
