import { render, screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { describe, expect, it, vi } from "vitest";

import { CreateItemForm } from "./create-item-form";

const createItemAction = vi.fn();

vi.mock("./actions", () => ({
  createItemAction: (...args: unknown[]) => createItemAction(...args),
}));

describe("CreateItemForm", () => {
  it("서버 액션이 돌려준 오류를 보여 준다", async () => {
    createItemAction.mockResolvedValue({ error: "이미 있는 이름입니다." });
    render(<CreateItemForm />);

    await userEvent.type(screen.getByPlaceholderText("이름"), "노트북");
    await userEvent.click(screen.getByRole("button", { name: "추가" }));

    expect(await screen.findByRole("alert")).toHaveTextContent("이미 있는 이름입니다.");
    const formData = createItemAction.mock.calls[0][1] as FormData;
    expect(formData.get("name")).toBe("노트북");
  });
});
